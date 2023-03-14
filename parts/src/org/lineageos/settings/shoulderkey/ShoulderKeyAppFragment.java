/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.shoulderkey;

import android.annotation.Nullable;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.util.Log;

import androidx.preference.PreferenceFragment;

import com.android.settingslib.applications.ApplicationsState;

import org.lineageos.settings.R;
import org.lineageos.settings.utils.SoundUtils;
import org.lineageos.settings.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoulderKeyAppFragment extends PreferenceFragment
        implements AdapterView.OnItemClickListener, ApplicationsState.Callbacks {

    private AllPackagesAdapter mAllPackagesAdapter;
    private ApplicationsState mApplicationsState;
    private ApplicationsState.Session mSession;
    private ActivityFilter mActivityFilter;
    private Map<String, ApplicationsState.AppEntry> mEntryMap =
            new HashMap<String, ApplicationsState.AppEntry>();

    private ShoulderKeyService mService;
    private ShoulderKeyUtils mUtils;

    private ListView mUserListView;
    private TextView mOrientationView;
    private int mRotation = 0;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        
        mService = ShoulderKeyService.getInstance();
        mUtils = mService.getShoulderKeyUtils();
        mRotation = mUtils.getRotationId();

        mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        mSession = mApplicationsState.newSession(this);
        mSession.onResume();
        mActivityFilter = new ActivityFilter(getActivity().getPackageManager());
        mAllPackagesAdapter = new AllPackagesAdapter(getActivity());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shoulderkey_app_layout, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        boolean isLandscape = mRotation == 1;
        MenuItem item = menu.add(Menu.NONE, 1, 0, isLandscape ? R.string.shoulder_key_app_landscape : R.string.shoulder_key_app_portrait);
        item.setIcon(isLandscape ? R.drawable.ic_landscape : R.drawable.ic_portrait);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(target -> {
            if (mRotation == 0) {
                item.setTitle(R.string.shoulder_key_app_landscape);
                item.setIcon(R.drawable.ic_landscape);
                mRotation = 1;
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mAllPackagesAdapter.notifyDataSetChanged();
            } else {
                item.setTitle(R.string.shoulder_key_app_portrait);
                item.setIcon(R.drawable.ic_portrait);
                mRotation = 0;
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mAllPackagesAdapter.notifyDataSetChanged();
            }
            return true;
        });
        MenuItem resetAll = menu.add(Menu.NONE, 2, 0, R.string.shoulder_key_app_reset);
        resetAll.setOnMenuItemClickListener(target -> {
            mUtils.resetAllAppsCoordinate();
            return true;
        });
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserListView = view.findViewById(R.id.app_list_view);
        mUserListView.setAdapter(mAllPackagesAdapter);
        mUserListView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        rebuild();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSession.onPause();
        mSession.onDestroy();
        mUtils.saveAllAppsCoordinate();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.coordinate.performClick();
    }

    @Override
    public void onPackageListChanged() {
        mActivityFilter.updateLauncherInfoList();
        rebuild();
    }

    @Override
    public void onRebuildComplete(ArrayList<ApplicationsState.AppEntry> entries) {
        if (entries != null) {
            handleAppEntries(entries);
            mAllPackagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadEntriesCompleted() {
        rebuild();
    }

    @Override
    public void onAllSizesComputed() {
    }

    @Override
    public void onLauncherInfoChanged() {
    }

    @Override
    public void onPackageIconChanged() {
    }

    @Override
    public void onPackageSizeChanged(String packageName) {
    }

    @Override
    public void onRunningStateChanged(boolean running) {
    }

    private void handleAppEntries(List<ApplicationsState.AppEntry> entries) {
        final ArrayList<String> sections = new ArrayList<String>();
        final ArrayList<Integer> positions = new ArrayList<Integer>();
        final PackageManager pm = getActivity().getPackageManager();
        String lastSectionIndex = null;
        int offset = 0;

        for (int i = 0; i < entries.size(); i++) {
            final ApplicationInfo info = entries.get(i).info;
            final String label = (String) info.loadLabel(pm);
            final String sectionIndex;

            if (!info.enabled) {
                sectionIndex = "--"; // XXX
            } else if (TextUtils.isEmpty(label)) {
                sectionIndex = "";
            } else {
                sectionIndex = label.substring(0, 1).toUpperCase();
            }

            if (lastSectionIndex == null ||
                    !TextUtils.equals(sectionIndex, lastSectionIndex)) {
                sections.add(sectionIndex);
                positions.add(offset);
                lastSectionIndex = sectionIndex;
            }

            offset++;
        }

        mAllPackagesAdapter.setEntries(entries, sections, positions);
        mEntryMap.clear();
        for (ApplicationsState.AppEntry e : entries) {
            mEntryMap.put(e.info.packageName, e);
        }
    }

    private void rebuild() {
        mSession.rebuild(mActivityFilter, ApplicationsState.ALPHA_COMPARATOR);
    }

    private static class ViewHolder {
        private TextView title;
        private TextView coordinate;
        private ImageView icon;
        private Switch appSwitch;
        private LinearLayout textLayout;
        private View rootView;

        private ViewHolder(View view) {
            this.title = view.findViewById(R.id.app_name);
            this.coordinate = view.findViewById(R.id.app_coordinate);
            this.icon = view.findViewById(R.id.app_icon);
            this.appSwitch = view.findViewById(R.id.app_switch);
            this.textLayout = view.findViewById(R.id.text_layout);
            this.rootView = view;

            view.setTag(this);
        }
    }

    private class AllPackagesAdapter extends BaseAdapter
            implements SectionIndexer, ShoulderKeyUtils.CoordinateChanageListener {

        private final LayoutInflater mInflater;
        private List<ApplicationsState.AppEntry> mEntries = new ArrayList<>();
        private String[] mSections;
        private int[] mPositions;

        public AllPackagesAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mActivityFilter = new ActivityFilter(context.getPackageManager());
            mUtils.setCoordinateChanageListener(this);
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public Object getItem(int position) {
            return mEntries.get(position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            return mEntries.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder(mInflater.inflate(
                        R.layout.shoulderkey_app_list_item, parent, false));
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ApplicationsState.AppEntry entry = mEntries.get(position);

            if (entry == null) {
                return holder.rootView;
            }

            int[][] coordinate = mUtils.loadCoordinate(entry.info.packageName, mRotation);
            if (coordinate != null) {
                String text = "L:";
                text += Arrays.toString(coordinate[0]);
                text += " R:";
                text += Arrays.toString(coordinate[1]);
                holder.coordinate.setText(text);
            } else {
                holder.coordinate.setText(R.string.shoulder_key_app_null);
            }

            holder.appSwitch.setChecked(mUtils.getEnabledByPackage(entry.info.packageName));

            holder.textLayout.setOnClickListener(v -> {
                mService.showKeyMappingViews(entry.info.packageName, mRotation, false);
            });

            holder.appSwitch.setOnClickListener(v -> {
                mUtils.setEnabledByPackage(entry.info.packageName, ((Switch)v).isChecked());
            });

            holder.title.setText(entry.label);
            mApplicationsState.ensureIcon(entry);
            holder.icon.setImageDrawable(entry.icon);
            return holder.rootView;
        }
        
        @Override
        public void onCoordinateChanged() {
            notifyDataSetChanged();
        }

        private void setEntries(List<ApplicationsState.AppEntry> entries,
                List<String> sections, List<Integer> positions) {
            mEntries = entries;
            mSections = sections.toArray(new String[sections.size()]);
            mPositions = new int[positions.size()];
            for (int i = 0; i < positions.size(); i++) {
                mPositions[i] = positions.get(i);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getPositionForSection(int section) {
            if (section < 0 || section >= mSections.length) {
                return -1;
            }

            return mPositions[section];
        }

        @Override
        public int getSectionForPosition(int position) {
            if (position < 0 || position >= getCount()) {
                return -1;
            }

            final int index = Arrays.binarySearch(mPositions, position);

            /*
             * Consider this example: section positions are 0, 3, 5; the supplied
             * position is 4. The section corresponding to position 4 starts at
             * position 3, so the expected return value is 1. Binary search will not
             * find 4 in the array and thus will return -insertPosition-1, i.e. -3.
             * To get from that number to the expected value of 1 we need to negate
             * and subtract 2.
             */
            return index >= 0 ? index : -index - 2;
        }

        @Override
        public Object[] getSections() {
            return mSections;
        }
    }

    private class ActivityFilter implements ApplicationsState.AppFilter {

        private final PackageManager mPackageManager;
        private final List<String> mLauncherResolveInfoList = new ArrayList<String>();

        private ActivityFilter(PackageManager packageManager) {
            this.mPackageManager = packageManager;

            updateLauncherInfoList();
        }

        public void updateLauncherInfoList() {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfoList = mPackageManager.queryIntentActivities(i, 0);

            synchronized (mLauncherResolveInfoList) {
                mLauncherResolveInfoList.clear();
                for (ResolveInfo ri : resolveInfoList) {
                    mLauncherResolveInfoList.add(ri.activityInfo.packageName);
                }
            }
        }

        @Override
        public void init() {
        }

        @Override
        public boolean filterApp(ApplicationsState.AppEntry entry) {
            boolean show = !mAllPackagesAdapter.mEntries.contains(entry.info.packageName);
            if (show) {
                synchronized (mLauncherResolveInfoList) {
                    show = mLauncherResolveInfoList.contains(entry.info.packageName);
                }
            }
            return show;
        }
    }
}
