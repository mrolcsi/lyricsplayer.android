package hu.mrolcsi.android.filebrowser;

import java.io.File;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2013.03.25.
 * Time: 22:14
 */

public abstract class Utils {

    private static final String[] ReservedChars = {"|", "\\", "?", "*", "<", "\"", ":", ">"};

    static String getExtension(String fileName) {
        String ext = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < fileName.length() - 1) ext = fileName.substring(i + 1).toLowerCase();
        return ext;
    }

    static String getNameWithoutExtension(String fileName) {
        int i = fileName.lastIndexOf('.');

        try {
            return fileName.substring(0, i);
        } catch (IndexOutOfBoundsException e) {
            return fileName;
        }
    }

    static List<File> sortByNameAsc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, new byFileName());
            Collections.sort(files, new byFileName());
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static List<File> sortByNameDesc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, Collections.reverseOrder(new byFileName()));
            Collections.sort(files, Collections.reverseOrder(new byFileName()));
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static List<File> sortByExtensionAsc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, new byFileName());
            Collections.sort(files, new byFileName());
            Collections.sort(files, new byExtension());
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static List<File> sortByExtensionDesc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, new byFileName());
            Collections.sort(files, new byFileName());
            Collections.sort(files, Collections.reverseOrder(new byExtension()));
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static List<File> sortByDateAsc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, new byDate());
            Collections.sort(files, new byDate());
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static List<File> sortByDateDesc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, Collections.reverseOrder(new byDate()));
            Collections.sort(files, Collections.reverseOrder(new byDate()));
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static List<File> sortBySizeAsc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, new bySize());
            Collections.sort(files, new bySize());
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static List<File> sortBySizeDesc(File[] input) {
        if (input != null) {
            List<File> files = new ArrayList<File>();
            List<File> dirs = new ArrayList<File>();
            for (File f : input) {
                if (f.isFile()) files.add(f);
                if (f.isDirectory()) dirs.add(f);
            }
            Collections.sort(dirs, Collections.reverseOrder(new bySize()));
            Collections.sort(files, Collections.reverseOrder(new bySize()));
            List<File> list = new ArrayList<File>();
            list.addAll(dirs);
            list.addAll(files);
            return list;
        } else return null;
    }

    static String getFriendlySize(File inputFile) {
        long rawSize = 0;
        if (inputFile.isFile()) {
            rawSize = inputFile.length();
        } else if (inputFile.isDirectory()) {
            rawSize = dirSize(inputFile);
        }
        if (rawSize > 1000000000) return String.format("%.2f GB", (float) rawSize / 1000000000);
        else if (rawSize > 1000000) return String.format("%.2f MB", (float) rawSize / 1000000);
        else if (rawSize > 1000) return String.format("%.2f kB", (float) rawSize / 1000);
        else return String.format("%d B", rawSize);
    }

    static String getFriendlySize(long rawSize) {
        if (rawSize > 1000000000) return String.format("%.2f GB", (float) rawSize / 1000000000);
        else if (rawSize > 1000000) return String.format("%.2f MB", (float) rawSize / 1000000);
        else if (rawSize > 1000) return String.format("%.2f kB", (float) rawSize / 1000);
        else return String.format("%d B", rawSize);
    }

    /**
     * Rekurzívan megadja egy mappa méretét byte-ban.
     *
     * @param dir Kinduló mappa.
     * @return A mappa mérete byte-ban.
     * @see <a href="http://stackoverflow.com/questions/4040912/how-can-i-get-the-size-of-a-folder-on-sd-card-in-android">forrás</a>
     */
    static long dirSize(File dir) {
        long result = 0;

        Stack<File> dirlist = new Stack<File>();
        dirlist.clear();

        dirlist.push(dir);

        while (!dirlist.isEmpty()) {
            File dirCurrent = dirlist.pop();

            File[] fileList = dirCurrent.listFiles();

            if (fileList != null) {
                for (File aFileList : fileList) {

                    if (aFileList.isDirectory())
                        dirlist.push(aFileList);
                    else
                        result += aFileList.length();
                }
            } else result = 0;
        }

        return result;
    }

    static boolean isFilenameValid(String filename) {
        for (String reservedChar : ReservedChars) {
            if (filename.contains(reservedChar)) return false;
        }
        return true;
    }
}

class byFileName implements Comparator<File> {
    @Override
    public int compare(File f1, File f2) {
        return f1.getName().compareToIgnoreCase(f2.getName());
    }
}

class byExtension implements Comparator<File> {

    @Override
    public int compare(File file, File file2) {
        String ext1 = Utils.getExtension(file.getName());
        String ext2 = Utils.getExtension(file2.getName());
        if (ext1 == null) return -1;
        if (ext2 == null) return 1;
        return ext1.compareToIgnoreCase(ext2);
    }
}

class byDate implements Comparator<File> {

    @Override
    public int compare(File f1, File f2) {
        long f1mod = f1.lastModified();
        long f2mod = f2.lastModified();
        if (f1mod < f2mod) return -1;
        else if (f1mod > f2mod) return 1;
        else return 0;
    }
}

class bySize implements Comparator<File> {

    @Override
    public int compare(File f1, File f2) {
        if (f1 == null) return 1;
        if (f2 == null) return -1;
        long f1size = 0, f2size = 0;
        if (f1.isFile() && f2.isFile()) {
            f1size = f1.length();
            f2size = f2.length();
        }
        if (f1.isDirectory() && f2.isDirectory()) {
            f1size = Utils.dirSize(f1);
            f2size = Utils.dirSize(f2);
        }
        if (f1size < f2size) return -1;
        else if (f1size > f2size) return 1;
        else return 0;
    }
}
