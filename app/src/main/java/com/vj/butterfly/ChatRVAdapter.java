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
                return INCOME;
            case OUTGOING:
                return OUTGOING;
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
        }
        return new ChatRVAdapter.ChatHolder(view);
        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_chats, parent, false);

        return new ChatRVAdapter.ChatHolder(view);*/
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatRVAdapter.ChatHolder holder, int position) {
        holder.tvMsg.setText(alDBMessages.get(position).get_message());
        holder.tvTime.setText(alDBMessages.get(position).get_time());
        if(holder.vMsgStatus!=null){
            switch (alDBMessages.get(position).get_status()){
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

        ChatHolder(View itemView) {
            super(itemView);
            tvMsg = itemView.findViewById(R.id.tv_msg);
            tvTime = itemView.findViewById(R.id.tv_time);
            if (itemView.findViewById(R.id.view_msg_status) != null)
                vMsgStatus = itemView.findViewById(R.id.view_msg_status);
        }
    }

}
