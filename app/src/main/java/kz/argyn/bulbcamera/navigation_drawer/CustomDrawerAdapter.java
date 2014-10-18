package kz.argyn.bulbcamera.navigation_drawer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kz.argyn.bulbcamera.R;

/**
 * Created by argyn on 25/07/2014.
 */
public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {

    private Context context;
    private int layoutResourceID;
    private List<DrawerItem> drawerItemsList;

    public CustomDrawerAdapter(Context context, int resource, List<DrawerItem> drawerItemsList) {
        super(context, resource, drawerItemsList);
        this.context = context;
        this.layoutResourceID = resource;
        this.drawerItemsList = drawerItemsList;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerItemHolder drawerHolder;
        View view = convertView;

        if(view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            view = inflater.inflate(layoutResourceID, parent, false);
            drawerHolder = new DrawerItemHolder();
            drawerHolder.itemName = (TextView) view.findViewById(R.id.drawer_itemName);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_icon);
            drawerHolder.drawerItem = (View) view.findViewById(R.id.drawer_item);
            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();
        }

        DrawerItem dItem = drawerItemsList.get(position);

        drawerHolder.icon.setImageDrawable(
                        view.getResources().getDrawable(dItem.getImageResourceID()));
        drawerHolder.itemName.setText(dItem.getItemName());

        return view;
    }

    private static class DrawerItemHolder {
        public TextView itemName;
        public ImageView icon;
        public View drawerItem;


    }
}
