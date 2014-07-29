package hu.mrolcsi.android.filebrowser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import hu.mrolcsi.android.lyricsplayer.R;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.widget.AdapterView.OnItemClickListener;
import static android.widget.AdapterView.OnItemLongClickListener;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2013.03.13.
 * Time: 16:49
 */

public class BrowserActivity extends Activity {

    //<editor-fold desc="Publics">
    /**
     * Fájl megnyitása
     *
     * @see #OPTION_BROWSE_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int MODE_OPEN_FILE = 67363453;
    /**
     * Mappa kiválasztása
     *
     * @see #OPTION_BROWSE_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int MODE_SELECT_DIR = 735328347;
    /**
     * Fájl mentése
     *
     * @see #OPTION_BROWSE_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int MODE_SAVE_FILE = 72833453;
    /**
     * Lista nézet
     *
     * @see #OPTION_LAYOUT
     */
    @SuppressWarnings("WeakerAccess")
    public static final int LAYOUT_LIST = 5478;
    /**
     * Négyzetrács (grid) nézet
     *
     * @see #OPTION_LAYOUT
     */
    @SuppressWarnings("WeakerAccess")
    public static final int LAYOUT_GRID = 4743;
    /**
     * Név szerint rendezés, növekvő
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_NAME_ASC = 1015610500;
    /**
     * Név szerint rendezés,. csökkenő
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_NAME_DESC = 1618270814;
    /**
     * Kiterjesztés szerint rendezés, növekvő
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_EXTENSION_ASC = 749124600;
    /**
     * Kiterjesztés szerint rendezés, csökkenő
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_EXTENSION_DESC = 1947142506;
    /**
     * Módosítás dátuma szerint rendezés, növekvő
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_DATE_ASC = -1712925401;
    /**
     * Módosítás dátuma szerint rendezés, csökkenő
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_DATE_DESC = -1361963493;
    /**
     * Méret szerint rendezés, növekvő
     * (Pontatlan, a nem olvasható mappák mérete 0 byte)
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_SIZE_ASC = -343875334;
    /**
     * Méret szerint rendezés, csökkenő
     * (Pontatlan, a nem olvasható mappák mérete 0 byte)
     *
     * @see #OPTION_SORT_MODE
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SORT_BY_SIZE_DESC = -1871084376;
    private static final int[] SORT_HASHES = new int[]{
            SORT_BY_NAME_ASC,
            SORT_BY_NAME_DESC,
            SORT_BY_EXTENSION_ASC,
            SORT_BY_EXTENSION_DESC,
            SORT_BY_DATE_ASC,
            SORT_BY_DATE_DESC,
            SORT_BY_SIZE_ASC,
            SORT_BY_SIZE_DESC
    };
    /**
     * Tallózás módja:
     * <ul>
     * <li>Fájl megnyitása: {@link #MODE_OPEN_FILE MODE_OPEN_FILE}</li>
     * <li>Mappa kiválasztása: {@link #MODE_SELECT_DIR MODE_SELECT_DIR}</li>
     * <li>Fájl mentése: {@link #MODE_SAVE_FILE MODE_SAVE_FILE}</li>
     * </ul>
     * (Alapértelmezett: fájl megnyitása)
     */
    @SuppressWarnings("WeakerAccess")
    public static final String OPTION_BROWSE_MODE;
    /**
     * String:  Kezdőmappa abszolút elérési útja (Alapértelmezett: SD-kártya gyökere, ha nincs, "/")
     */
    @SuppressWarnings("WeakerAccess")
    public static final String OPTION_START_PATH;
    /**
     * String:  Engedélyezett kiterjesztések pontosvesszővel (;) elválasztva (Alapértelmezett: üres)
     */
    @SuppressWarnings("WeakerAccess")
    public static final String OPTION_EXTENSION_FILTER;
    /**
     * Visszatérési érték: a kiválasztott fájl/mappa abszolút elérési útja
     * onActivityResult metódusban használandó, mint getStringExtra paraméter.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String RESULT;
    /**
     * Rendezés módja (mappák mindig elöl)
     * <ul>
     * <li>Név szerint növekvő: {@link #SORT_BY_NAME_ASC SORT_BY_NAME_ASC}</li>
     * <li>Név szerint csökkenő: {@link #SORT_BY_NAME_DESC SORT_BY_NAME_DESC}</li>
     * <li>Kiterjesztés szerint növekvő: {@link #SORT_BY_EXTENSION_ASC SORT_BY_EXTENSION_ASC}</li>
     * <li>Kiterjesztés szerint csökkenő: {@link #SORT_BY_EXTENSION_DESC SORT_BY_EXTENSION_DESC}</li>
     * <li>Módosítás dátuma szerint növekvő: {@link #SORT_BY_DATE_ASC SORT_BY_DATE_ASC}</li>
     * <li>Módosítás dátuma szerint csökkenő: {@link #SORT_BY_DATE_DESC SORT_BY_DATE_DESC}</li>
     * <li>Méret szerint növekvő: {@link #SORT_BY_SIZE_ASC SORT_BY_SIZE_ASC}</li>
     * <li>Méret szerint növekvő: {@link #SORT_BY_SIZE_DESC SORT_BY_SIZE_DESC}</li>
     * </ul>
     * (Alapértelmezett: fájlnév szerint növekvő)
     */
    @SuppressWarnings("WeakerAccess")
    public static final String OPTION_SORT_MODE;
    /**
     * String:  Alapértelmezett fájlnév, csak fájlmentéskor van rá szükség.
     *
     * @see #OPTION_BROWSE_MODE
     * @see #MODE_SAVE_FILE
     */
    @SuppressWarnings("WeakerAccess")
    public static final String OPTION_DEFAULT_FILENAME;
    /**
     * Boolean: A kiindulópontként megadott mappát kezelje-e gyökérként? (boolean)
     *
     * @see #OPTION_START_PATH
     */
    @SuppressWarnings("WeakerAccess")
    public static final String OPTION_START_IS_ROOT;
    /**
     * Kezdeti elrendezés (futás közben váltogatható)
     * <ul>
     * <li>Lista {@link #LAYOUT_LIST LAYOUT_LIST}</li>
     * <li>Négyzetrácsos(grid) {@link #LAYOUT_GRID LAYOUT_GRID}</li>
     * </ul>
     * Alapértelmezett: lista.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String OPTION_LAYOUT;
    static {
        OPTION_START_IS_ROOT = "startIsRoot";
        OPTION_DEFAULT_FILENAME = "defaultFileName";
        OPTION_SORT_MODE = "sort";
        RESULT = "result";
        OPTION_EXTENSION_FILTER = "extensionFilter";
        OPTION_START_PATH = "startPath";
        OPTION_BROWSE_MODE = "browseMode";
        OPTION_LAYOUT = "layout";
    }
    //</editor-fold>
    //<editor-fold desc="Privates">
    private static final int ERROR_FOLDER_NOT_READABLE = -394829994;
    private static final int ERROR_CANT_CREATE_FOLDER = -227013011;
    private static final int ERROR_INVALID_FILENAME = -1490604826;
    private static final int ERROR_INVALID_FOLDERNAME = -1336390888;
    private String currentPath;
    private AbsListView list;
    private Intent resultIntent;
    private int browseMode;
    private int sortMode;
    private String[] extensionFilter;
    private String defaultFileName;
    private String startPath;
    private boolean startIsRoot;
    private int activeLayout;
    private TextView tvCurrentPath;
    private boolean shortPress;
    private int itemLayoutID;
    private MenuItem menuViewAs;
    private EditText etFilename;
    private ViewFlipper vf;
    private Map<String, Parcelable> states = new ConcurrentHashMap<String, Parcelable>();
    private boolean cancelIsFirstPress = true;
    //</editor-fold>

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //<editor-fold desc="Változók inicializálása az Intentből">
        if (savedInstanceState == null) {
            final Intent inputIntent = getIntent();
            browseMode = inputIntent.getIntExtra(OPTION_BROWSE_MODE, MODE_OPEN_FILE);
            startPath = inputIntent.getStringExtra(OPTION_START_PATH) != null ? inputIntent.getStringExtra(OPTION_START_PATH) : Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/";
            currentPath = startPath;
            if (inputIntent.getStringExtra(OPTION_EXTENSION_FILTER) != null)
                extensionFilter = inputIntent.getStringExtra(OPTION_EXTENSION_FILTER).split(";");
            sortMode = inputIntent.getIntExtra(OPTION_SORT_MODE, SORT_BY_NAME_ASC);
            defaultFileName = inputIntent.getStringExtra(OPTION_DEFAULT_FILENAME);
            startIsRoot = inputIntent.getBooleanExtra(OPTION_START_IS_ROOT, true);
            activeLayout = inputIntent.getIntExtra(OPTION_LAYOUT, LAYOUT_LIST);
            switch (activeLayout) {
                default:
                case LAYOUT_LIST:
                    itemLayoutID = R.layout.browser_listitem_layout;
                    break;
                case LAYOUT_GRID:
                    itemLayoutID = R.layout.browser_griditem_layout;
                    break;
            }
        } else onRestoreInstanceState(savedInstanceState);
        //</editor-fold>

        switch (browseMode) {
            default:
            case MODE_OPEN_FILE:
                setContentView(R.layout.browser_layout);
                setTitle(R.string.browser_titleOpenFile);
                break;
            case MODE_SELECT_DIR:
                setContentView(R.layout.browser_layout);
                setTitle(R.string.browser_titleSelectDir);
                break;
            case MODE_SAVE_FILE:
                setContentView(R.layout.browser_layout_save);
                setTitle(R.string.browser_titleSaveFile);
                ImageButton imgbtnSave = (ImageButton) findViewById(R.id.browser_imageButtonSave);
                etFilename = (EditText) findViewById(R.id.browser_editTextFilename);
                if (defaultFileName != null) etFilename.setText(defaultFileName);
                imgbtnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String fileName = currentPath + "/" + etFilename.getText();
                        if (!fileName.equals("") && Utils.isFilenameValid(fileName)) {
                            File f = new File(fileName);
                            if (f.exists()) {
                                showOverwriteDialog(fileName);
                            } else {
                                ok(fileName);
                            }
                        } else {
                            showErrorDialog(ERROR_INVALID_FILENAME);
                        }
                    }
                });
                break;
        }
        tvCurrentPath = (TextView) findViewById(R.id.browser_textViewCurrentDir);
        vf = (ViewFlipper) findViewById(R.id.browser_viewFlipper);

        switch (activeLayout) {
            default:
            case LAYOUT_LIST:
                toListView();
                break;
            case LAYOUT_GRID:
                toGridView();
                break;
        }

        /**
         * API level 11 és fölötte menü helyett ActionBar.
         */
        if (Build.VERSION.SDK_INT >= 11) {
            ActionBar actionBar = getActionBar();
            switch (browseMode) {
                default:
                case MODE_OPEN_FILE:
                    actionBar.setTitle(R.string.browser_titleOpenFile);
                    actionBar.setIcon(R.drawable.browser_folder_open);
                    break;
                case MODE_SELECT_DIR:
                    actionBar.setTitle(R.string.browser_titleSelectDir);
                    actionBar.setIcon(R.drawable.browser_folder_open);
                    break;
                case MODE_SAVE_FILE:
                    actionBar.setTitle(R.string.browser_titleSaveFile);
                    actionBar.setIcon(R.drawable.browser_save_title);
                    break;
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Változók aktuális állapotának mentése.
     *
     * @param outState Állapotot tároló Bundle.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("currentPath", currentPath);
        outState.putInt(OPTION_BROWSE_MODE, browseMode);
        outState.putInt(OPTION_SORT_MODE, sortMode);
        outState.putStringArray(OPTION_EXTENSION_FILTER, extensionFilter);
        outState.putString(OPTION_START_PATH, startPath);
        outState.putBoolean(OPTION_START_IS_ROOT, startIsRoot);
        outState.putInt(OPTION_LAYOUT, activeLayout);
        outState.putInt("itemLayoutID", itemLayoutID);
        outState.putString(OPTION_DEFAULT_FILENAME, defaultFileName);
        super.onSaveInstanceState(outState);
    }

    /**
     * Mentett állapot visszatöltése változókba.
     *
     * @param savedInstanceState Állapotot tároló Bundle
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        startPath = savedInstanceState.getString(OPTION_START_PATH, "/");
        currentPath = savedInstanceState.getString("currentPath", startPath);
        browseMode = savedInstanceState.getInt(OPTION_BROWSE_MODE, MODE_OPEN_FILE);
        sortMode = savedInstanceState.getInt(OPTION_SORT_MODE, SORT_BY_NAME_ASC);
        extensionFilter = savedInstanceState.getStringArray(OPTION_EXTENSION_FILTER);
        startIsRoot = savedInstanceState.getBoolean(OPTION_START_IS_ROOT, true);
        activeLayout = savedInstanceState.getInt(OPTION_LAYOUT, LAYOUT_LIST);
        itemLayoutID = savedInstanceState.getInt("itemLayoutID");
        defaultFileName = savedInstanceState.getString(OPTION_DEFAULT_FILENAME);
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * View váltás után listenerek újraregisztrálása.
     */
    private void setListListeners() {
        switch (browseMode) {
            default:
            case MODE_OPEN_FILE:
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.getAbsolutePath().equals("/" + getString(R.string.browser_upFolder))) {
                            loadList(new File(currentPath).getParentFile());
                        } else {
                            if (holder.file.isDirectory()) loadList(holder.file);
                            if (holder.file.isFile()) {
                                ok(holder.file.getAbsolutePath());
                            }
                        }
                    }
                });
                list.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.isFile()) {
                            ok(holder.file.getAbsolutePath());
                        }
                        return holder.file.isFile();
                    }
                });
                break;
            case MODE_SELECT_DIR:
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.getAbsolutePath().equals("/" + getString(R.string.browser_upFolder))) {
                            loadList(new File(currentPath).getParentFile());
                        } else if (holder.file.isDirectory()) loadList(holder.file);
                    }
                });
                list.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.isDirectory())
                            ok(holder.file.getAbsolutePath());
                        return true;
                    }
                });
                break;
            case MODE_SAVE_FILE:
                list.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.getAbsolutePath().equals("/" + getString(R.string.browser_upFolder))) {
                            loadList(new File(currentPath).getParentFile());
                        } else {
                            if (holder.file.isFile()) etFilename.setText(holder.file.getName());
                            if (holder.file.isDirectory()) loadList(holder.file);
                        }
                    }
                });
                list.setOnItemLongClickListener(new OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (!holder.file.isFile()) return false;
                        else {
                            showOverwriteDialog(holder.file.getAbsolutePath());
                            return true;
                        }
                    }
                });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menuViewAs = menu.findItem(R.id.browser_menu_viewAs);
        switch (activeLayout) {
            default:
            case LAYOUT_LIST:
                menuViewAs.setIcon(R.drawable.browser_view_as_grid);
                menuViewAs.setTitle(R.string.browser_menu_viewAsGrid);
                break;
            case LAYOUT_GRID:
                menuViewAs.setIcon(R.drawable.browser_view_as_list);
                menuViewAs.setTitle(R.string.browser_menu_viewAsList);
                break;
        }

        File currentDir = new File(currentPath);
        MenuItem menuNewFolder = menu.findItem(R.id.browser_menu_newFolder);
        menuNewFolder.setVisible(currentDir.canWrite());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            cancel();

        } else if (i == R.id.browser_menu_viewAs) {
            setLayout();

        } else if (i == R.id.browser_menu_newFolder) {
            createNewFolder();

        } else if (i == R.id.browser_menu_sortBy) {
            showSortDialog();

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Dialógus megjelenítése a rendezési mód kiválasztásához.
     */
    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this)
                .setTitle(R.string.browser_menu_sortBy)
                .setIcon(R.drawable.browser_sort_by)
                .setItems(R.array.browser_sortOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sortMode = SORT_HASHES[i];
                        loadList(new File(currentPath));
                    }
                });
        AlertDialog ad = builder.create();
        ad.show();
    }

    /**
     * Új mappa létrehozása az aktuális mappában.
     * WRITE_EXTERNAL_STORAGE szükséges!
     */
    private void createNewFolder() {
        final View view = getLayoutInflater().inflate(R.layout.browser_dialog_newfolder, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this)
                .setTitle(R.string.browser_menu_newFolder)
                .setIcon(R.drawable.browser_new_folder).setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText etFolderName = (EditText) view.findViewById(R.id.browser_etNewFolder);
                        if (Utils.isFilenameValid(etFolderName.getText().toString())) {
                            File newDir = new File(currentPath + "/" + etFolderName.getText());
                            if (newDir.mkdir()) {
                                loadList(new File(currentPath));
                            } else showErrorDialog(ERROR_CANT_CREATE_FOLDER);
                        } else showErrorDialog(ERROR_INVALID_FOLDERNAME);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setView(view);
        AlertDialog ad = builder.create();
        ad.show();
    }

    /**
     * Váltás Lista és Grid nézet között.
     */
    private void setLayout() {
        switch (activeLayout) {
            default:
            case LAYOUT_LIST:
                toGridView();
                menuViewAs.setIcon(R.drawable.browser_view_as_list);
                menuViewAs.setTitle(R.string.browser_menu_viewAsList);
                break;
            case LAYOUT_GRID:
                toListView();
                menuViewAs.setIcon(R.drawable.browser_view_as_grid);
                menuViewAs.setTitle(R.string.browser_menu_viewAsGrid);
                break;
        }
        setListListeners();
        states = new ConcurrentHashMap<String, Parcelable>();
        loadList(new File(currentPath));
    }

    /**
     * Lista nézetbe váltás ViewFlipper segítségével.
     */
    private void toListView() {
        vf.setDisplayedChild(0);
        activeLayout = LAYOUT_LIST;
        list = (ListView) findViewById(R.id.browser_listView);
        itemLayoutID = R.layout.browser_listitem_layout;
        setListListeners();
        loadList(new File(currentPath));
    }

    /**
     * Grid nézetbe váltás ViewFlipperen keresztül.
     */
    private void toGridView() {
        vf.setDisplayedChild(1);
        activeLayout = LAYOUT_GRID;
        list = (GridView) findViewById(R.id.browser_gridView);
        itemLayoutID = R.layout.browser_griditem_layout;
        setListListeners();
        loadList(new File(currentPath));
    }

    //<editor-fold desc="Vissza gomb lekezelése: rövid nyomás: egy mappával feljebb | hoszú nyomás: kilépés tallózásból">
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getRepeatCount() == 0) {
                shortPress = true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            shortPress = false;
            cancel();
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (shortPress) {
                if (!currentPath.equals((startIsRoot) ? startPath : "/")) {
                    loadList(new File(currentPath).getParentFile());
                    cancelIsFirstPress = true;
                } else {
                    //Toast.makeText(this, getString(R.string.browser_toast_noParentDir), Toast.LENGTH_SHORT).show();
                    if (cancelIsFirstPress) {
                        Toast.makeText(this, getString(R.string.browser_pressAgainToCancel), Toast.LENGTH_SHORT).show();
                        cancelIsFirstPress = false;
                    } else cancel();
                }
            }
            shortPress = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    //</editor-fold>

    /**
     * Visszatérés a kiválasztott fájl/mappa teljes elérési útjával.
     *
     * @param path A fő Activityben ezt az elérési utat kell felhasználni.
     */
    private void ok(String path) {
        resultIntent = new Intent();
        resultIntent.putExtra(BrowserActivity.RESULT, path);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Fájlok listájának betöltése a ListView/GridView-ba.
     *
     * @param directory A betöltendő mappa.
     */
    @SuppressWarnings("ConstantConditions")
    private void loadList(final File directory) {
        if (!directory.canRead()) {
            showErrorDialog(ERROR_FOLDER_NOT_READABLE);
            return;
        }

        states.put(currentPath, list.onSaveInstanceState());

        File[] filesToLoad;

        if (extensionFilter != null) filesToLoad = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isFile()) {
                    String ext = Utils.getExtension(file.getName());
                    int i = 0;
                    int n = extensionFilter.length;
                    while (i < n && !extensionFilter[i].toLowerCase().equals(ext)) i++;
                    return i < n;
                } else return file.canRead();
            }
        });
        else filesToLoad = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.canRead();
            }
        });

        currentPath = directory.getAbsolutePath();
        tvCurrentPath.setText(currentPath);

        FileListAdapter fla;
        boolean isRoot = startIsRoot ? currentPath.equals(startPath) : currentPath.equals("/");

        switch (browseMode) {
            default:
            case MODE_SAVE_FILE:
            case MODE_OPEN_FILE:
                fla = new FileListAdapter(this, itemLayoutID, filesToLoad, sortMode, isRoot);
                break;
            case MODE_SELECT_DIR:
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                };
                fla = new FileListAdapter(this, itemLayoutID, directory.listFiles(filter), sortMode, isRoot);
                break;
        }


        //API Level 11 alatt castolni kell...
        switch (activeLayout) {
            case LAYOUT_GRID:
                //noinspection RedundantCast
                ((GridView) list).setAdapter(fla);
                break;
            case LAYOUT_LIST:
                //noinspection RedundantCast
                ((ListView) list).setAdapter(fla);
                break;
        }
        if (Build.VERSION.SDK_INT >= 11) {
            this.invalidateOptionsMenu();
        }
        //if (browseMode == MODE_SAVE_FILE) imgbtnSave.setEnabled(directory.canWrite());
        Parcelable state = states.get(currentPath);
        if (state != null)
            list.onRestoreInstanceState(state);
    }

    /**
     * Ha a mentéskor megadott névvel már létezik fájl, megerősítést kér a felülírásról.
     * Tényleges írás NEM történik.
     *
     * @param fileName fájlnév
     */
    private void showOverwriteDialog(final String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(BrowserActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.browser_fileExists_message)
                .setTitle(R.string.browser_fileExists_title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ok(fileName);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog ad = builder.create();
        ad.show();
    }

    /**
     * Hibaüzenet megjelenítése a felhasználónak.
     *
     * @param error a hiba oka
     */
    private void showErrorDialog(int error) {
        AlertDialog.Builder builder = null;
        switch (error) {
            case ERROR_CANT_CREATE_FOLDER:
                builder = new AlertDialog.Builder(BrowserActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.browser_error_cantCreateFolder_message)
                        .setTitle(R.string.browser_error_cantCreateFolder_title)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                break;
            case ERROR_FOLDER_NOT_READABLE:
                builder = new AlertDialog.Builder(BrowserActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.browser_error_folderCantBeOpened_message)
                        .setTitle(R.string.browser_error_folderCantBeOpened_title)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                break;
            case ERROR_INVALID_FILENAME:
                builder = new AlertDialog.Builder(BrowserActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.browser_error_invalidFilename_message)
                        .setTitle(R.string.browser_error_invalidFilename_title)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                break;
            case ERROR_INVALID_FOLDERNAME:
                builder = new AlertDialog.Builder(BrowserActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.browser_error_invalidFolderName_message)
                        .setTitle(R.string.browser_error_invalidFolderName_title)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                break;
            default:
                break;
        }

        AlertDialog ad = builder != null ? builder.create() : null;
        if (ad != null) {
            ad.show();
        }
    }

    /**
     * Tallózás mégsézése, visszatérés RESULT_CANCELED üzenettel.
     */
    private void cancel() {
        setResult(RESULT_CANCELED, resultIntent);
        finish();
    }
}