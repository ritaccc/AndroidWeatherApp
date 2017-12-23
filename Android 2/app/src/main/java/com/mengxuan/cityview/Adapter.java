package com.mengxuan.cityview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mengxuan.cityview.weatherUtils.Weather;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.CityViewHolder> {
    DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
    private static final String TAG = Adapter.class.getSimpleName();
    private ArrayList<City> mCityItems;
    private ArrayList<String> mCityNames;
    private final ListItemClickListener mOnClickListener;
    private final ListItemRemoveClickListener mOnRemoveClickListener;
    private boolean isC = true;
    private String myLocationName = "";
    public Adapter(ListItemClickListener listener, ListItemRemoveClickListener removeClickListener, boolean isC) {
        this.mCityItems = new ArrayList<>();
        this.mCityNames = new ArrayList<>();
        mOnClickListener = listener;
        mOnRemoveClickListener = removeClickListener;
        this.isC = isC;
    }
    public void addNewCitry(City city) {
        if (!mCityNames.contains(city.name)) {
            mCityItems.add(city);
            mCityNames.add(city.name);
        }
    }
    public void setIsC(boolean isC) {
        this.isC = isC;
    }

    public void setMyLocationName(String myLocationName) {
        this.myLocationName = myLocationName;
    }

    public String getMyLocationName() {
        return this.myLocationName;
    }

    public City getCity(int position) {
        return mCityItems.get(position);
    }

    public ArrayList<City> getCityList() {
        return mCityItems;
    }

    public void removeCity(int position) {
        String cityName = mCityNames.get(position);
        mCityNames.remove(cityName);
        for(int i =0;i<mCityItems.size();i++) {
            if(mCityItems.get(i).name.equals(cityName)){
                mCityItems.remove(i);
            }
        }
    }

    @Override
    public CityViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.city_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        CityViewHolder viewHolder = new CityViewHolder(view);
        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(CityViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        holder.bind(mCityItems.get(position));
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return mCityItems.size();
    }


    interface ListItemClickListener {
        void onListItemClick(int number);
    }

    interface ListItemRemoveClickListener {
        void onListItemRemoveClick(int number);
    }
    /**
     * Cache of the children views for a list item.
     */
    class CityViewHolder extends RecyclerView.ViewHolder{

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView listItemCityView;
        Button listItemRemoveButton;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link Adapter#onCreateViewHolder(ViewGroup, int)}
         */
        public CityViewHolder(View itemView) {
            super(itemView);

            listItemCityView = (TextView) itemView.findViewById(R.id.tv_item_number);
            listItemRemoveButton = (Button) itemView.findViewById(R.id.tv_item_remove);
            listItemCityView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickListener.onListItemClick(getAdapterPosition());
                }
            });
            listItemRemoveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnRemoveClickListener.onListItemRemoveClick(getAdapterPosition());
                }
            });

        }

        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the appropriate text within a list item.
         * @param city cityName
         */
        void bind(City city) {
            DateTime currentTime = DateTime.now( DateTimeZone.forID(city.timeZoneId) );
            String timeString = currentTime.toString(dtf);
            listItemCityView.setText(city.name + " " + timeString + " " + Weather.getTempString(city.weather.temp,isC));
        }
    }
}
