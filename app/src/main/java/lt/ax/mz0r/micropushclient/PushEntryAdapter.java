package lt.ax.mz0r.micropushclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by m00n on 12/22/16.
 */

public class PushEntryAdapter extends RecyclerView.Adapter<PushEntryAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView titleView;
        TextView messageView;
        TextView dateView;
        ImageView iconView;

        Boolean bound = false;

        ViewHolder(View itemView) {
            super(itemView);
            
            this.titleView = (TextView) itemView.findViewById(R.id.messageTitle);
            this.messageView = (TextView) itemView.findViewById(R.id.messageBody);
            this.dateView = (TextView) itemView.findViewById(R.id.messageDate);
            this.iconView = (ImageView) itemView.findViewById(R.id.messageIcon);

            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater inflater = new MenuInflater(v.getContext());
            inflater.inflate(R.menu.menu_list_context, menu);
        }

        public void clearAnimation()
        {
            this.itemView.clearAnimation();
        }
    }

    private List<PushEntry> items;
    private Context context;
    private Integer position; // Implementin' shit that was done in ListView. Yay.
    private int lastItemCount = -1;
    private Boolean animations = false;
    private AdapterView.OnItemClickListener clickListener;

    public PushEntryAdapter(Context context, List<PushEntry> items, AdapterView.OnItemClickListener clickListener) {
        this.items = items;
        this.context = context;
        this.clickListener = clickListener;
    }

    public void enableAnimations() {
        animations = true;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public PushEntryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.pushmessage_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final PushEntryAdapter.ViewHolder holder, final int position) {
        PushEntry pushEntry = this.items.get(position);

        holder.titleView.setText(pushEntry.getTitle());
        holder.messageView.setText(pushEntry.getMessage());

        Date date = pushEntry.getDate();
        Date now = new Date();
        SimpleDateFormat dateFormatter;

        Calendar cdate = Calendar.getInstance();
        Calendar cnow = Calendar.getInstance();
        cdate.setTime(date);
        cnow.setTime(now);

        if (cdate.get(Calendar.DAY_OF_YEAR) == cnow.get(Calendar.DAY_OF_YEAR) &&
            cdate.get(Calendar.YEAR) == cnow.get(Calendar.YEAR)) {
            dateFormatter = new SimpleDateFormat("HH:mm");
        } else {
            dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        }
        holder.dateView.setText(dateFormatter.format(date));

        holder.iconView.setImageBitmap(Globals.getInstance(context).getIcons().get(pushEntry.getIcon()));

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(holder.getAdapterPosition());
                return false;
            }
        });
        if (this.clickListener != null) {
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(null, v, position, 0);
                }
            });
        }
        setAnimation(holder.itemView, position, holder);
        if (!holder.bound) {
            holder.bound = true;
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        holder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public void onViewDetachedFromWindow(final ViewHolder holder)
    {
        ((ViewHolder)holder).clearAnimation();
    }

    private void setAnimation(View viewToAnimate, int position, ViewHolder h)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        //Log.d("FOO", "setanimation " + position + " : " + lastItemCount + " : " + (getItemCount() - 1) + " : " + h.bound);


        if (position == 0 && lastItemCount < getItemCount() && animations)
        {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
            viewToAnimate.startAnimation(animation);
            //lastItemCount = position;
        }

        lastItemCount = getItemCount();
        /*
        if (position > lastItemCount) {
            lastItemCount = position;
        }*/
    }
    /*
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        PushEntry pushEntry = getItem(position);

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pushmessage_layout, null);
            holder = new ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.messageTitle);
            holder.messageView = (TextView) convertView.findViewById(R.id.messageBody);
            holder.dateView = (TextView) convertView.findViewById(R.id.messageDate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.titleView.setText(pushEntry.getTitle());
        holder.messageView.setText(pushEntry.getMessage());

        Date date = pushEntry.getDate();
        Date now = new Date();
        SimpleDateFormat dateFormatter;

        Calendar cdate = Calendar.getInstance();
        Calendar cnow = Calendar.getInstance();
        cdate.setTime(date);
        cnow.setTime(now);

        if (cdate.get(Calendar.DAY_OF_YEAR) == cnow.get(Calendar.DAY_OF_YEAR) &&
            cdate.get(Calendar.YEAR) == cnow.get(Calendar.YEAR)) {
            dateFormatter = new SimpleDateFormat("HH:mm");
        } else {
            dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        }
        holder.dateView.setText(dateFormatter.format(date));

        return convertView;
    }*/
}
