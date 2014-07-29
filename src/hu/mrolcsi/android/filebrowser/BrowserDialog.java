package hu.mrolcsi.android.filebrowser;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import hu.mrolcsi.android.lyricsplayer.R;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2013.07.30.
 * Time: 11:34
 */

public class BrowserDialog extends DialogFragment {


    //<editor-fold desc="Publics">
    /**
     * Fájl megnyitása
     *
     * @see #OPTION_BROWSE_MODE
     */
    public static final int MODE_OPEN_FILE = 67363453;
    private int browseMode = MODE_OPEN_FILE;
    /**
     * Mappa kiválasztása
     *
     * @see #OPTION_BROWSE_MODE
     */
    public static final int MODE_SELECT_DIR = 735328347;
    /**
     * Fájl mentése
     *
     * @see #OPTION_BROWSE_MODE
     */
    public static final int MODE_SAVE_FILE = 72833453;
    /**
     * Lista nézet
     *
     * @see #OPTION_LAYOUT
     */
    public static final int LAYOUT_LIST = 5478;
    private int activeLayout = LAYOUT_LIST;
    /**
     * Négyzetrács (grid) nézet
     *
     * @see #OPTION_LAYOUT
     */
    public static final int LAYOUT_GRID = 4743;
    /**
     * Név szerint rendezés, növekvő
     *
     * @see #OPTION_SORT_MODE
     */
    public static final int SORT_BY_NAME_ASC = 1015610500;
    private int sortMode = SORT_BY_NAME_ASC;
    /**
     * Név szerint rendezés,. csökkenő
     *
     * @see #OPTION_SORT_MODE
     */
    public static final int SORT_BY_NAME_DESC = 1618270814;
    /**
     * Kiterjesztés szerint rendezés, növekvő
     *
     * @see #OPTION_SORT_MODE
     */
    public static final int SORT_BY_EXTENSION_ASC = 749124600;
    /**
     * Kiterjesztés szerint rendezés, csökkenő
     *
     * @see #OPTION_SORT_MODE
     */
    public static final int SORT_BY_EXTENSION_DESC = 1947142506;
    /**
     * Módosítás dátuma szerint rendezés, növekvő
     *
     * @see #OPTION_SORT_MODE
     */
    public static final int SORT_BY_DATE_ASC = -1712925401;
    /**
     * Módosítás dátuma szerint rendezés, csökkenő
     *
     * @see #OPTION_SORT_MODE
     */
    public static final int SORT_BY_DATE_DESC = -1361963493;
    /**
     * Méret szerint rendezés, növekvő
     * (Pontatlan, a nem olvasható mappák mérete 0 byte)
     *
     * @see #OPTION_SORT_MODE
     */
    public static final int SORT_BY_SIZE_ASC = -343875334;
    /**
     * Méret szerint rendezés, csökkenő
     * (Pontatlan, a nem olvasható mappák mérete 0 byte)
     *
     * @see #OPTION_SORT_MODE
     */
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
    public static final String OPTION_BROWSE_MODE;
    /**
     * String:  Kezdőmappa abszolút elérési útja (Alapértelmezett: SD-kártya gyökere, ha nincs, "/")
     */
    public static final String OPTION_START_PATH;
    /**
     * String:  Engedélyezett kiterjesztések pontosvesszővel (;) elválasztva (Alapértelmezett: üres)
     */
    public static final String OPTION_EXTENSION_FILTER;
    /**
     * Visszatérési érték: a kiválasztott fájl/mappa abszolút elérési útja
     * onActivityResult metódusban használandó, mint getStringExtra paraméter.
     */
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
    public static final String OPTION_SORT_MODE;
    /**
     * String:  Alapértelmezett fájlnév, csak fájlmentéskor van rá szükség.
     *
     * @see #OPTION_BROWSE_MODE
     * @see #MODE_SAVE_FILE
     */
    public static final String OPTION_DEFAULT_FILENAME;
    /**
     * Boolean: A kiindulópontként megadott mappát kezelje-e gyökérként? (boolean)
     *
     * @see #OPTION_START_PATH
     */
    public static final String OPTION_START_IS_ROOT;
    /**
     * Kezdeti elrendezés (futás közben váltogatható)
     * <ul>
     * <li>Lista {@link #LAYOUT_LIST LAYOUT_LIST}</li>
     * <li>Négyzetrácsos(grid) {@link #LAYOUT_GRID LAYOUT_GRID}</li>
     * </ul>
     * Alapértelmezett: lista.
     */
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
    public static final String TAG = "hu.mrolcsi.android.filebrowser.browserdialog";
    //</editor-fold>
    //<editor-fold desc="Privates">
    private static final int ERROR_FOLDER_NOT_READABLE = -394829994;
    private static final int ERROR_CANT_CREATE_FOLDER = -227013011;
    private static final int ERROR_INVALID_FILENAME = -1490604826;
    private static final int ERROR_INVALID_FOLDERNAME = -1336390888;
    private AbsListView list;
    private String[] extensionFilter;
    private String defaultFileName;
    private String startPath = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/";
    private String currentPath = startPath;
    private boolean startIsRoot = true;
    private TextView tvCurrentPath;
    private int itemLayoutID = R.layout.browser_listitem_layout;
    private ImageButton imgbtnSave;
    private EditText etFilename;
    private ViewFlipper vf;

    private View rootView;
    private OnDialogResultListener onDialogResultListener = new OnDialogResultListener() {
        @Override
        public void onPositiveResult(String path) {
        }

        @Override
        public void onNegativeResult() {
        }
    };
    private Map<String, Parcelable> states = new ConcurrentHashMap<String, Parcelable>();
    private ImageButton btnSwitchLayout;
    private ImageButton btnSortMode;
    private ImageButton btnNewFolder;
    private boolean overwrite = false;
    //</editor-fold>


    public BrowserDialog() {
        super();
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            startPath = savedInstanceState.getString(OPTION_START_PATH, "/");
            currentPath = savedInstanceState.getString("currentPath", startPath);
            browseMode = savedInstanceState.getInt(OPTION_BROWSE_MODE, MODE_OPEN_FILE);
            sortMode = savedInstanceState.getInt(OPTION_SORT_MODE, SORT_BY_NAME_ASC);
            extensionFilter = savedInstanceState.getStringArray(OPTION_EXTENSION_FILTER);
            startIsRoot = savedInstanceState.getBoolean(OPTION_START_IS_ROOT, true);
            activeLayout = savedInstanceState.getInt(OPTION_LAYOUT, LAYOUT_LIST);
            itemLayoutID = savedInstanceState.getInt("itemLayoutID");
            defaultFileName = savedInstanceState.getString(OPTION_DEFAULT_FILENAME);
        }
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        switch (browseMode) {
            default:
            case MODE_OPEN_FILE:
            case MODE_SELECT_DIR:
                rootView = inflater.inflate(R.layout.browser_layout_dialog, container, false);
                break;
            case MODE_SAVE_FILE:
                rootView = inflater.inflate(R.layout.browser_layout_dialog_save, container, false);
                imgbtnSave = (imgbtnSave == null) ? (ImageButton) rootView.findViewById(R.id.browser_imageButtonSave) : imgbtnSave;
                etFilename = (etFilename == null) ? (EditText) rootView.findViewById(R.id.browser_editTextFileName) : etFilename;
                if (defaultFileName != null) etFilename.setText(defaultFileName);
                imgbtnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String fileName = currentPath + "/" + etFilename.getText();
                        if (!fileName.isEmpty() && Utils.isFilenameValid(fileName)) {
                            File f = new File(fileName);
                            if (f.exists()) {
                                if (!overwrite) {
                                    Toast.makeText(getActivity(), "Press again to overwrite file.", Toast.LENGTH_SHORT).show();
                                    overwrite = true;
                                    //TODO: ellenőrizni
                                } else {
                                    onDialogResultListener.onPositiveResult(fileName);
                                    dismiss();
                                }
                            } else {
                                onDialogResultListener.onPositiveResult(fileName);
                                dismiss();
                            }
                        } else {
                            showErrorDialog(ERROR_INVALID_FILENAME);
                        }
                    }
                });
                break;
        }

        btnSwitchLayout = (ImageButton) rootView.findViewById(R.id.btnLayout);
        btnSwitchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayout();
            }
        });

        btnSortMode = (ImageButton) rootView.findViewById(R.id.btnSort);
        btnSortMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSortDialog();
            }
        });

        btnNewFolder = (ImageButton) rootView.findViewById(R.id.btnNewFolder);
        btnNewFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewFolderDialog();
            }
        });

        tvCurrentPath = (TextView) rootView.findViewById(R.id.browser_textViewCurrentDir);

        vf = (ViewFlipper) rootView.findViewById(R.id.browser_viewFlipper);
        switch (activeLayout) {
            default:
            case LAYOUT_LIST:
                toListView();
                break;
            case LAYOUT_GRID:
                toGridView();
                break;
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
     * Váltás Lista és Grid nézet között.
     */
    private void setLayout() {
        switch (activeLayout) {
            default:
            case LAYOUT_LIST:
                toGridView();
                break;
            case LAYOUT_GRID:
                toListView();
                break;
        }
        setListListeners();
        states = new ConcurrentHashMap<String, Parcelable>();
        loadList(new File(currentPath));
    }

    /**
     * Lista nézetbe váltás ViewFlipperen keresztül.
     */
    private void toListView() {
        vf.setDisplayedChild(0);
        activeLayout = LAYOUT_LIST;
        btnSwitchLayout.setImageResource(R.drawable.browser_view_as_grid);
        list = (ListView) rootView.findViewById(R.id.browser_listView);
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
        btnSwitchLayout.setImageResource(R.drawable.browser_view_as_list);
        list = (GridView) rootView.findViewById(R.id.browser_gridView);
        itemLayoutID = R.layout.browser_griditem_layout;
        setListListeners();
        loadList(new File(currentPath));
    }

    /**
     * Dialógus megjelenítése a rendezési mód kiválasztásához.
     */
    private void showSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
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
     * View váltás után listenerek újraregisztrálása.
     */
    private void setListListeners() {
        switch (browseMode) {
            default:
            case MODE_OPEN_FILE:
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.getAbsolutePath().equals("/" + getString(R.string.browser_upFolder))) {
                            loadList(new File(currentPath).getParentFile());
                        } else {
                            if (holder.file.isDirectory()) loadList(holder.file);
                            if (holder.file.isFile()) {
                                onDialogResultListener.onPositiveResult(holder.file.getAbsolutePath());
                                dismiss();
                            }
                        }
                    }
                });
                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.isFile()) {
                            onDialogResultListener.onPositiveResult(holder.file.getAbsolutePath());
                            dismiss();
                        }
                        return holder.file.isFile();
                    }
                });
                break;
            case MODE_SELECT_DIR:
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.getAbsolutePath().equals("/" + getString(R.string.browser_upFolder))) {
                            loadList(new File(currentPath).getParentFile());
                        } else if (holder.file.isDirectory()) loadList(holder.file);
                    }
                });
                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (holder.file.isDirectory()) {
                            onDialogResultListener.onPositiveResult(holder.file.getAbsolutePath());
                            dismiss();
                        }
                        return true;
                    }
                });
                break;
            case MODE_SAVE_FILE:
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                        FileHolder holder = (FileHolder) view.getTag();
                        if (!holder.file.isFile()) return false;
                        else {
                            showOverwriteDialog(holder.file.getAbsolutePath());
                            Toast.makeText(getActivity(), "Press Save button twice to overwrite file.", Toast.LENGTH_LONG).show();
                            return true;
                        }
                    }
                });

        }
    }

    /**
     * Fájlok listájának betöltése a ListView/GridView-ba.
     *
     * @param directory A betöltendő mappa.
     */
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
                fla = new FileListAdapter(getActivity(), itemLayoutID, filesToLoad, sortMode, isRoot);
                break;
            case MODE_SELECT_DIR:
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                };
                fla = new FileListAdapter(getActivity(), itemLayoutID, directory.listFiles(filter), sortMode, isRoot);
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
        //if (browseMode == MODE_SAVE_FILE) imgbtnSave.setEnabled(directory.canWrite());
        Parcelable state = states.get(currentPath);
        if (state != null)
            list.onRestoreInstanceState(state);

        File currentFile = new File(currentPath);
        btnNewFolder.setVisibility(currentFile.canWrite() ? View.VISIBLE : View.GONE);
    }

    /**
     * Ha a mentéskor megadott névvel már létezik fájl, megerősítést kér a felülírásról.
     * Tényleges írás NEM történik.
     *
     * @param fileName fájlnév
     */
    private void showOverwriteDialog(final String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(R.string.browser_fileExists_message)
                .setTitle(R.string.browser_fileExists_title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        onDialogResultListener.onPositiveResult(fileName);
                        dismiss();
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
     * Új mappa létrehozása az aktuális mappában.
     * WRITE_EXTERNAL_STORAGE szükséges!
     */
    private void showNewFolderDialog() {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.browser_dialog_newfolder, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
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
     * Hibaüzenet megjelenítése a felhasználónak.
     *
     * @param error a hiba oka
     */
    private void showErrorDialog(int error) {
        AlertDialog.Builder builder = null;
        switch (error) {
            case ERROR_CANT_CREATE_FOLDER:
                builder = new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.browser_error_cantCreateFolder_message)
                        .setTitle(R.string.browser_error_cantCreateFolder_title)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
//                Toast.makeText(getActivity(), R.string.browser_error_cantCreateFolder_message, Toast.LENGTH_LONG).show();
                break;
            case ERROR_FOLDER_NOT_READABLE:
                builder = new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.browser_error_folderCantBeOpened_message)
                        .setTitle(R.string.browser_error_folderCantBeOpened_title)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
//                Toast.makeText(getActivity(), R.string.browser_error_folderCantBeOpened_message, Toast.LENGTH_LONG).show();
                break;
            case ERROR_INVALID_FILENAME:
//                builder = new AlertDialog.Builder(getActivity())
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .setMessage(R.string.browser_error_invalidFilename_message)
//                        .setTitle(R.string.browser_error_invalidFilename_title)
//                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                            }
//                        });
//                Toast.makeText(getActivity(), R.string.browser_error_invalidFilename_message, Toast.LENGTH_LONG).show();
                break;
            case ERROR_INVALID_FOLDERNAME:
                builder = new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(R.string.browser_error_invalidFolderName_message)
                        .setTitle(R.string.browser_error_invalidFolderName_title)
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
//                Toast.makeText(getActivity(), R.string.browser_error_invalidFolderName_message, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }

        AlertDialog ad = builder != null ? builder.create() : null;
        if (ad != null) {
            ad.show();
        }
    }

    public void setOnDialogResultListener(OnDialogResultListener listener) {
        this.onDialogResultListener = listener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        onDialogResultListener.onNegativeResult();
    }

    //<editor-fold desc="GETTERS & SETTERS">
    @SuppressWarnings("UnusedDeclaration")
    public int getBrowseMode() {
        return browseMode;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BrowserDialog setBrowseMode(int browseMode) {
        this.browseMode = browseMode;
        return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getSortMode() {
        return sortMode;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BrowserDialog setSortMode(int sortMode) {
        this.sortMode = sortMode;
        return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String[] getExtensionFilter() {
        return extensionFilter;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BrowserDialog setExtensionFilter(String extensionFilter) {
        this.extensionFilter = extensionFilter.split(";");
        return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getDefaultFileName() {
        return defaultFileName;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BrowserDialog setDefaultFileName(String defaultFileName) {
        this.defaultFileName = defaultFileName;
        return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getStartPath() {
        return startPath;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BrowserDialog setStartPath(String startPath) {
        this.startPath = startPath;
        return this;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean isStartRoot() {
        return startIsRoot;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BrowserDialog setStartIsRoot(boolean startIsRoot) {
        this.startIsRoot = startIsRoot;
        return this;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    @SuppressWarnings("UnusedDeclaration")
    public BrowserDialog setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
        return this;
    }
    //</editor-fold>

    public interface OnDialogResultListener {
        /**
         * Visszatérés a kiválasztott fájl/mappa teljes elérési útjával.
         *
         * @param path A hívó Activityben felhasználható elérési út.
         */
        public abstract void onPositiveResult(String path);

        /**
         * Nem lett kiválasztva fájl/mappa.
         * A dialógus bezárult.
         */
        public abstract void onNegativeResult();
    }
}
