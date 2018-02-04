package com.phoenix.answer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.BiFunction;

import com.phoenix.answer.model.Model;

public final class ModelUtil {

	private ModelUtil(){}
	
	public static List<Model> findKthUsingHeap(Collection<Model> models,
			Comparator<Model> comparator,
			BiFunction<Model, Model, Boolean> function, int topK) {
		PriorityQueue<Model> queue = new PriorityQueue<Model>(topK, comparator);
		int i = 0;

		Iterator<Model> iter = models.iterator();
		while (i < topK && iter.hasNext()) {
			queue.add(iter.next());
			i++;
		}
		for (; i < models.size() && iter.hasNext(); i++) {
			Model value = queue.peek();
			Model newVal = iter.next();
			if (function.apply(newVal, value)) {
				queue.poll();
				queue.add(newVal);
			}
		}

		List<Model> result = new ArrayList<>(topK);
		while (queue.size() > 0) {
			result.add(queue.poll());
		}

		Collections.reverse(result);
		return result;
	}
}
