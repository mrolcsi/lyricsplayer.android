package hu.mrolcsi.android.filebrowser;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import hu.mrolcsi.android.lyricsplayer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Matusinka Roland
 * Date: 2013.03.16.
 * Time: 16:17
 */

class FileListAdapter extends BaseAdapter {

    private final int layoutResourceId;
    private final Context context;
    private final int sortMode;
    private final LayoutInflater inflater;
    private List<File> data = null;

    public FileListAdapter(Context context, int layoutResourceId, File[] inputData, int sortMode, boolean isRoot) {
        super();
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.sortMode = sortMode;
        this.inflater = ((Activity) context).getLayoutInflater();

        data = new ArrayList<File>();
        if (!isRoot) data.add(new File(context.getString(R.string.browser_upFolder)));


//        if (inputData.length > 0) {
        switch (this.sortMode) {
            default:
            case BrowserActivity.SORT_BY_NAME_ASC:
                this.data.addAll(Utils.sortByNameAsc(inputData));
                break;
            case BrowserActivity.SORT_BY_NAME_DESC:
                this.data.addAll(Utils.sortByNameDesc(inputData));
                break;
            case BrowserActivity.SORT_BY_EXTENSION_ASC:
                this.data.addAll(Utils.sortByExtensionAsc(inputData));
                break;
            case BrowserActivity.SORT_BY_EXTENSION_DESC:
                this.data.addAll(Utils.sortByExtensionDesc(inputData));
                break;
            case BrowserActivity.SORT_BY_DATE_ASC:
                this.data.addAll(Utils.sortByDateAsc(inputData));
                break;
            case BrowserActivity.SORT_BY_DATE_DESC:
                this.data.addAll(Utils.sortByDateDesc(inputData));
                break;
            case BrowserActivity.SORT_BY_SIZE_ASC:
                this.data.addAll(Utils.sortBySizeAsc(inputData));
                break;
            case BrowserActivity.SORT_BY_SIZE_DESC:
                this.data.addAll(Utils.sortBySizeDesc(inputData));
                break;
        }
//        } else {
//            data.add(new File(context.getString(R.string.browser_emptyFolder)));
//        }
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FileHolder holder;

        if (row == null) {
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FileHolder();
            holder.icon = (ImageView) row.findViewById(R.id.browser_listItemIcon);
            holder.text = (TextView) row.findViewById(R.id.browser_listItemText);
            holder.extra = (TextView) row.findViewById(R.id.browser_listItemExtra);

            row.setTag(holder);
        } else holder = (FileHolder) row.getTag();

        holder.file = data.get(position);
        boolean isUp = holder.file.getAbsolutePath().equals("/..");

        if (isUp) {
            holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.browser_up));
        } else {
            if (holder.file.isDirectory()) {
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.browser_folder_closed));
            }
            if (holder.file.isFile()) {
                holder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.browser_file));
            }
        }

        switch (sortMode) {
            default:
            case BrowserActivity.SORT_BY_NAME_ASC:
            case BrowserActivity.SORT_BY_NAME_DESC:
                holder.text.setText(holder.file.getName());
                if (holder.extra != null) holder.extra.setText(null);
                break;
            case BrowserActivity.SORT_BY_EXTENSION_ASC:
            case BrowserActivity.SORT_BY_EXTENSION_DESC:
                if (holder.file.isFile() && Utils.getExtension(holder.file.getName()) != null) {
                    holder.text.setText(Utils.getNameWithoutExtension(holder.file.getName()));
                    if (holder.extra != null)
                        holder.extra.setText(new StringBuilder().append(".").append(Utils.getExtension(holder.file.getName())));
                } else {
                    holder.text.setText(holder.file.getName());
                    if (holder.extra != null) holder.extra.setText(null);
                }
                break;
            case BrowserActivity.SORT_BY_DATE_ASC:
            case BrowserActivity.SORT_BY_DATE_DESC:
                holder.text.setText(holder.file.getName());
                if (!isUp && holder.extra != null)
                    holder.extra.setText(String.format("%1$tY.%1$tm.%1$td\n" +
                            "%1$tH:%1$tM", holder.file.lastModified()));
                break;
            case BrowserActivity.SORT_BY_SIZE_ASC:
            case BrowserActivity.SORT_BY_SIZE_DESC:
//                AsyncTask<Void, Long, Long> calculateSizeTask = new AsyncTask<Void, Long, Long>() {
//
//                    @Override
//                    protected Long doInBackground(Void... voids) {
//                        long result = 0;
//
//                        Stack<File> dirlist = new Stack<File>();
//                        dirlist.clear();
//
//                        dirlist.push(holder.file);
//
//                        while (!dirlist.isEmpty()) {
//                            File dirCurrent = dirlist.pop();
//
//                            File[] fileList = dirCurrent.listFiles();
//
//                            if (fileList != null) {
//                                for (File aFileList : fileList) {
//
//                                    if (aFileList.isDirectory())
//                                        dirlist.push(aFileList);
//                                    else {
//                                        result += aFileList.length();
//                                        publishProgress(aFileList.length());
//                                    }
//                                }
//                            } else result = 0;
//                        }
//                        return result;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Long aLong) {
//                        super.onPostExecute(aLong);
//
//                    }
//
//                    @Override
//                    protected void onProgressUpdate(Long... values) {
//                        super.onProgressUpdate(values);
//                        holder.extra.setText(Utils.getFriendlySize(values[0]));
//                    }
//                };
                holder.text.setText(holder.file.getName());
                if (holder.extra != null) {
                    //calculateSizeTask.execute();
                    if (!isUp && holder.file.isDirectory()) {
                        int count;
                        try {
                            count = holder.file.listFiles().length;
                            holder.extra.setText(context.getResources().getQuantityString(R.plurals.browser_numberOfFiles, count, count) + "\n" + Utils.getFriendlySize(holder.file));
                        } catch (NullPointerException e) {
                            holder.extra.setText(R.string.browser_unknown);
                        }
                    } else if (holder.file.isFile()) holder.extra.setText(Utils.getFriendlySize(holder.file));
                }
                break;
        }
        return row;
    }

}

class FileHolder {
    ImageView icon;
    TextView text;
    TextView extra;
    File file;
}