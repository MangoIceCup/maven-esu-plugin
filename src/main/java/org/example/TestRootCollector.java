package org.example;

import org.example.entity.TestRoot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class TestRootCollector implements Collector<TestRoot, List<List<TestRoot>>, List<List<TestRoot>>> {
    private final int bulkSize;

    public TestRootCollector(int bulkSize) {
        if (bulkSize <= 0) {
            throw new IllegalArgumentException("bulk size should > 0");
        }
        this.bulkSize = bulkSize;
    }

    @Override
    public Supplier<List<List<TestRoot>>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<List<TestRoot>>, TestRoot> accumulator() {
        return (a, t) -> {
            if (a.isEmpty() || a.get(a.size() - 1).size() == bulkSize) {
                ArrayList<TestRoot> arrayList = new ArrayList<>();
                a.add(arrayList);
            }
            a.get(a.size() - 1).add(t);
        };
    }

    @Override
    public BinaryOperator<List<List<TestRoot>>> combiner() {
        return (a, b) -> {
            ArrayList<List<TestRoot>> arrayList = new ArrayList<>();
            for (int i = 0; i < a.size() - 1; i++) {
                arrayList.add(a.get(i));
            }
            for (int i = 0; i < b.size() - 1; i++) {
                arrayList.add(b.get(i));
            }
            ArrayList<TestRoot> single = new ArrayList<>();
            if (!a.isEmpty()) {
                single.addAll(a.get(a.size() - 1));
            }
            final int needSize = bulkSize - single.size();
            if (!b.isEmpty()) {
                final List<TestRoot> bLast = b.get(b.size() - 1);
                if (needSize >= bLast.size()) {
                    single.addAll(bLast);
                } else {
                    single.addAll(bLast.subList(0, needSize));
                }
            }
            if (!single.isEmpty()) {
                arrayList.add(single);
            }
            if (!b.isEmpty()) {
                final List<TestRoot> bLast = b.get(b.size() - 1);
                if (bLast.size() > needSize) {
                    arrayList.add(new ArrayList<>(bLast.subList(needSize, bLast.size())));
                }
            }
            return arrayList;
        };
    }

    @Override
    public Function<List<List<TestRoot>>, List<List<TestRoot>>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        final HashSet<Characteristics> characteristics = new HashSet<>();
        characteristics.add(Characteristics.IDENTITY_FINISH);
        return characteristics;
    }
}
