package io.palaima.bluetooth.app;


import com.palaima.bluetooth.app.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.palaima.bluetooth.Device;

public class DevicesAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Device> mDevices;

    public DevicesAdapter(Context context, List<Device> devices) {
        this.mContext = context;
        this.mDevices = devices;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = convertView;
        ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.list_device, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        Device device = mDevices.get(position);
        holder.mNameTV.setText(device.getName());
        holder.mMacTV.setText(device.getAddress());

        return view;
    }

    private class ViewHolder {
        public TextView mNameTV;
        public TextView mMacTV;

        private ViewHolder(View view) {
            mNameTV = (TextView) view.findViewById(R.id.nameTV);
            mMacTV = (TextView) view.findViewById(R.id.macTV);
        }
    }
}
