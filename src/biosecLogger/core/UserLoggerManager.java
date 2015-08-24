package biosecLogger.core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import android.content.Context;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import biosecLogger.exceptions.ExistingUserException;
import biosecLogger.exceptions.InvalidLoginException;
import biosecLogger.exceptions.PatternMismatchException;

/**
 * UserLoggerManager is main controller to work with BiosecEvaluation. Creates
 * new template, evaluates login with template. Handles logger, service and
 * handles storage.
 * 
 * @author Stefan Smihla
 * 
 */
public enum UserLoggerManager {
	INSTANCE;

	/** Log name is tag for logger */
	public static final String LOGNAME = "BiosecLogger";

	/** Returned on method success */
	public static final int CORRECT = 0;

	/** Returned when new user is created (without template) */
	public static final int CREATED = 1;

	/** Returned when user model is updated during registration process */
	public static final int UPDATED = 2;

	/** Returned when registration is completed */
	public static final int COMPLETED = 3;

	/** Returned when external storage is fully accessible */
	public static final int WRITABLE = 0;

	/** Returned when external storage is read only */
	public static final int READABLE = 1;

	/** Returned when external storage is not present */
	public static final int UNMOUNTED = 2;

	protected static final String EXTENSION = ".log";
	protected static final String SETTINGS_FILE = "settings.ini";

	private UserModel user;
	private Logger logger;
	private CompositeOnKeyListener listeners;
	private OptionsManager oManager;

	private EditText editText;
	private Context ctx;

	/**
	 * Returns instance of UserLoggerManager.
	 * 
	 * @param ctx
	 *            activity context
	 * @param editText
	 *            password editText field
	 * @param oManager
	 *            evaluation settings
	 * @return instance of UserLoggerManager
	 */
	public static UserLoggerManager getInstance(Context ctx, EditText editText,
			OptionsManager oManager) {
		INSTANCE.initInstance(ctx, editText, oManager);
		return INSTANCE;
	}

	/**
	 * Initialize instance from beginning.
	 * 
	 * @param ctx
	 *            activity context
	 * @param editText
	 *            password editText field
	 * @param oManager
	 *            evaluation settings
	 */
	private void initInstance(Context ctx, EditText editText,
			OptionsManager oManager) {
		this.user = null;
		this.logger = Logger.getInstance(ctx);
		this.oManager = oManager;

		this.listeners = new CompositeOnKeyListener();
		this.listeners.addListener(logger.addFlyingOnKeyListener());
		this.editText = editText;
		this.ctx = ctx;

		editText.addTextChangedListener(logger);
		editText.setOnKeyListener(listeners);
	}

	/**
	 * Loads user from internal storage into UserModel instance.
	 * 
	 * @param username
	 *            name of user
	 * @param password
	 *            encrypted user's password
	 * @return true on success else false
	 * @throws IOException
	 *             raises when file does not exist, or couldn't be read
	 * @throws InvalidLoginException
	 *             raises when password does not match
	 */
	private boolean loadUser(String username, String password)
			throws IOException, InvalidLoginException {
		String template;
		try {
			template = StorageHandler.loadFile(username + EXTENSION, ctx);
		} catch (FileNotFoundException e) {
			/* User not exists */
			throw new InvalidLoginException();
		}

		user = new UserModel(null, null, oManager.getTemplateHoldCounter());
		return user.loadTemplateFromString(template, password);
	}

	/**
	 * Encrypt password with sha256 algorithm. This is used to store password.
	 * 
	 * @param password
	 *            plain password phrase
	 * @return encrypted password phrase
	 */
	private String encrypt(String password) {
		return new String(
				Hex.encodeHex(DigestUtils.sha256(password.getBytes())));
	}

	/**
	 * Adds biometric sample to user model.
	 */
	private void saveLoggedValues() {
		user.addRow(logger.submit());
	}

	/**
	 * Adds external on key listener to edit text.
	 * 
	 * @param externalListener
	 *            listener to be added
	 */
	public void addExternalOnKeyListener(OnKeyListener externalListener) {
		if (externalListener != null) {
			listeners.addListener(externalListener);
			editText.setOnKeyListener(listeners);
		}
	}

	/**
	 * Returns number of created samples for template.
	 * 
	 * @return counter samples count
	 */
	public int getCounter() {
		return (user != null) ? user.getCounter() : 0;
	}

	/**
	 * Submit new sample during registration.
	 * 
	 * @param username
	 *            name of new user
	 * @param password
	 *            password of new user
	 * @return CREATED, UPDATED or COMPLETED constant
	 * @throws InvalidLoginException
	 *             raises when password does not match
	 * @throws ExistingUserException
	 *             raises when user already exists in storage
	 * @throws IOException
	 *             raises when new user couldn't be saved
	 */
	public int submitSample(String username, String password)
			throws ExistingUserException, IOException, InvalidLoginException {
		password = encrypt(password);
		editText.setText("");

		/* Test if user exists, we expect that FileNotFoundException */
		try {
			StorageHandler.loadFile(username + EXTENSION, ctx);
			logger.initValues();
			throw new ExistingUserException();
		} catch (FileNotFoundException e) {
		}

		if (user == null) {
			user = new UserModel(username, password,
					oManager.getTemplateHoldCounter());
			saveLoggedValues();
			return CREATED;

		} else if (user.checkUser(username, password) == true) {
			saveLoggedValues();
			if (user.getCounter() == oManager.getTemplateCreateCounter()) {
				saveTemplate();
				stopLogging();
				return COMPLETED;
			} else {
				return UPDATED;
			}

		} else {
			logger.initValues();
			throw new InvalidLoginException();
		}
	}

	/**
	 * Save template to internal (and optionally external) storage.
	 * 
	 * @throws IOException
	 *             raises when file couldn't be saved
	 */
	private void saveTemplate() throws IOException {
		String template = user.saveTemplateToString();
		StorageHandler.saveFile(user.getUsername() + EXTENSION, template, ctx,
				false);
		if (oManager.checkExternalSaving()) {
			StorageHandler.saveFile(user.getUsername() + EXTENSION, template,
					null, false);
		}
		stopLogging();
	}

	/**
	 * Continue with logging.
	 */
	public void resumeLogging() {
		logger.startLogging();
	}

	/**
	 * Pause logging.
	 */
	public void stopLogging() {
		logger.stopLogging();
	}

	/**
	 * Removes internal storage data.
	 */
	public void removeDataFiles() {
		StorageHandler.removeDataFiles(ctx);
	}

	/**
	 * Checks if external storage is presented.
	 * 
	 * @return actual state (WRITABLE, READABLE, UNMOUNTED)
	 */
	public int checkExternalStorage() {
		return StorageHandler.checkEnvironment();
	}

	/**
	 * Submits and evaluates user during login process.
	 * 
	 * @param username
	 *            name of the user
	 * @param password
	 *            password of the user
	 * @return CORRECT on success
	 * @throws PatternMismatchException
	 *             raises when LoginEvalutor evaluates pattern mismatch
	 * @throws InvalidLoginException
	 *             raises when passwords does not match
	 * @throws IOException
	 *             raises when there is problem with read/write to file
	 */
	public int submitUser(String username, String password)
			throws PatternMismatchException, IOException, InvalidLoginException {
		password = encrypt(password);
		editText.setText("");

		try {
			loadUser(username, password);
		} catch (InvalidLoginException e) {
			logger.initValues();
			throw new InvalidLoginException();
		}

		List<LoggedKey> row = logger.submit();
		LoginEvaluator loginEvaluator = new LoginEvaluator(oManager);

		if (loginEvaluator.checkPatter(user, row)) {
			user.addRow(row);
			saveTemplate();
			stopLogging();
			return CORRECT;
		} else {
			logger.initValues();
			saveTemplate();
			throw new PatternMismatchException();
		}
	}
}