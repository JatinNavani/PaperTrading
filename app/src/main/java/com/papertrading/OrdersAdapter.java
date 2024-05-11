package com.papertrading;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<Order> orders;

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderTypeTextView;
        private TextView quantityTextView;
        private TextView tradingSymbolTextView;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTypeTextView = itemView.findViewById(R.id.order_type_text_view);
            quantityTextView = itemView.findViewById(R.id.quantity_text_view);
            tradingSymbolTextView = itemView.findViewById(R.id.trading_symbol_text_view);
        }

        public void bind(Order order) {
            orderTypeTextView.setText(order.getOrderType());
            quantityTextView.setText(String.valueOf(order.getQuantity()));
            tradingSymbolTextView.setText(order.getTradingSymbol());
        }
    }
}
