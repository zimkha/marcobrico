package com.mmd.marcobrico.service;

import com.mmd.marcobrico.domain.Order;

import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

public class OrderService {

    public double totalOrder(List<Order> orders) {
        return orders.stream()
                .filter(Order::isPaid)
                .mapToDouble(Order::getAmount)
                .sum();
    }
    public List<Order> getListOrderGreatherThanAmount(List<Order> orders, double minAmount) {
        return orders.stream()
                .filter(order -> order.getAmount() > minAmount)
                .toList();
    }


    static DoubleUnaryOperator discount = amount -> amount * 0.9;
    static DoubleUnaryOperator tax = a -> a * 1.2;
    DoubleUnaryOperator pipeline = discount.andThen(tax);

    static Function<Double, String> format = amount -> String.format("%.2f €", amount);



    public static String getProcessPrice(double amount) {
        return format.apply(
                discount.andThen(tax)
                        .applyAsDouble(amount)
        );
    }
    public String getName() {
        List<String> names = List.of(
                "Alice", "Bob", "Charlie", "David",
                "Alex", "Brian", "Amanda"
        );
        return names.stream()
                .filter(name -> name.startsWith("A"))
                .sorted(Comparator.comparing(String::length))
                .map(String::toUpperCase)
                .collect(Collectors.joining(", "));
    }
    public static List<Integer> transform(
            List<Integer> list,
            Function<Integer, Integer> transformer
    ) {
        return list.stream()
                .mapToInt(Integer::intValue)
                .map((IntUnaryOperator) transformer)
                .boxed().toList();
    }
}
