package com.vj.butterfly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import app_utility.DataBaseHelper;
import app_utility.StaticReferenceClass;

/*
 * Created by Vj on 20-APR-19.
 */

class ChatRVAdapter extends RecyclerView.Adapter<ChatRVAdapter.ChatHolder> {

    private static final int INCOME = 1;
    private static final int OUTGOING = 2;

    private static final int DATE = 3;

    public static int viewType;
    private Context context;

    private RecyclerView recyclerView;

    private ArrayList<DataBaseHelper> alDBMessages;

    ChatRVAdapter(Context context, RecyclerView recyclerView, ArrayList<DataBaseHelper> alDBMessages) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.alDBMessages = alDBMessages;
    }

    @Override
    public int getItemViewType(int position) {
        switch (this.alDBMessages.get(position).get_type()) {
            case INCOME:
                viewType = INCOME;
                return INCOME;
            case OUTGOING:
                viewType = OUTGOING;
                return OUTGOING;
            case DATE:
                viewType = DATE;
                return DATE;
        }
        return -1;
    }

    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater;
        View view = null;
        switch (viewType) {
            case INCOME:
                inflater = LayoutInflater.from(parent.getContext());
                view = inflater.inflate(R.layout.item_message_received, parent, false);
                break;
            case OUTGOING:
                inflater = LayoutInflater.from(parent.getContext());
                view = inflater.inflate(R.layout.item_message_sent, parent, false);
                break;
            case DATE:
                inflater = LayoutInflater.from(parent.getContext());
                view = inflater.inflate(R.layout.date_layout, parent, false);
                break;
        }
        return new ChatRVAdapter.ChatHolder(view);
        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_chats, parent, false);

        return new ChatRVAdapter.ChatHolder(view);*/
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatRVAdapter.ChatHolder holder, int position) {

        switch (viewType) {
            case DATE:
                holder.tvDate.setText(alDBMessages.get(position).get_time());
                break;
            default:
                holder.tvMsg.setText(alDBMessages.get(position).get_message());
                //Log.e("ChatRVAdapter", alDBMessages.get(position).get_time().split(" ")[0]);
                //String[] saTime = alDBMessages.get(position).get_time().split(" ");
                String sTime = alDBMessages.get(position).get_time().split(" ")[1];
                holder.tvTime.setText(sTime);
                if (holder.vMsgStatus != null) {
                    switch (alDBMessages.get(position).get_status()) {
                        case StaticReferenceClass.PENDING:
                            holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                            break;
                        case StaticReferenceClass.SENT:
                            holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                            break;
                        case StaticReferenceClass.DELIVERED:
                            holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(R.color.colorNextToWhite));
                            break;
                        case StaticReferenceClass.READ:
                            holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(R.color.colorBlue));
                            break;
                    }
                }
                break;
        }
       /* holder.tvMsg.setText(alDBMessages.get(position).get_message());
        //Log.e("ChatRVAdapter", alDBMessages.get(position).get_time().split(" ")[0]);
        //String[] saTime = alDBMessages.get(position).get_time().split(" ");
        String sTime = alDBMessages.get(position).get_time().split(" ")[1];
        holder.tvTime.setText(sTime);
        if (holder.vMsgStatus != null) {
            switch (alDBMessages.get(position).get_status()) {
                case StaticReferenceClass.PENDING:
                    holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                    break;
                case StaticReferenceClass.SENT:
                    holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                    break;
                case StaticReferenceClass.DELIVERED:
                    holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(R.color.colorNextToWhite));
                    break;
                case StaticReferenceClass.READ:
                    holder.vMsgStatus.setBackgroundColor(context.getResources().getColor(R.color.colorBlue));
                    break;
            }
        }*/
    }

    @Override
    public int getItemCount() {
        return alDBMessages.size();
    }

    public List<DataBaseHelper> getDataset() {
        return alDBMessages;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ChatHolder extends RecyclerView.ViewHolder {
        TextView tvMsg;
        TextView tvTime;
        View vMsgStatus;
        TextView tvDate;

        ChatHolder(View itemView) {
            super(itemView);
            if (ChatRVAdapter.viewType == DATE) {
                tvDate = itemView.findViewById(R.id.tv_date);
            } else {
                tvMsg = itemView.findViewById(R.id.tv_msg);
                tvTime = itemView.findViewById(R.id.tv_time);
                if (itemView.findViewById(R.id.view_msg_status) != null)
                    vMsgStatus = itemView.findViewById(R.id.view_msg_status);
            }
        }
    }

}
