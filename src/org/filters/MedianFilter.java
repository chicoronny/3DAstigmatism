package org.filters;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MedianFilter<T extends Comparable<T>> {

	// ////////////////////////////////////////////
	// // Fast median
	// // from Battiato and al, An efficient algorithm for the approximate
	// median selection problem
	private void adjustTriplet(List<T> a, final int i, final int step) {
		int j = i + step;
		int k = i + 2;

		if (a.get(i).compareTo(a.get(j)) < 0) {
			if (a.get(k).compareTo(a.get(i)) < 0)
				Collections.swap(a, i, j);
			else if (a.get(k).compareTo(a.get(j)) < 0)
				Collections.swap(a, j, k);
		} else {
			if (a.get(i).compareTo(a.get(k)) < 0)
				Collections.swap(a, i, j);
			else if (a.get(k).compareTo(a.get(j)) > 0)
				Collections.swap(a, j, k);
		}
	}

	private void selectionSort(List<T> a, final int left, final int size,
			final int step) {
		int min;
		for (int i = left; i < left + (size - 1) * step; i += step) {
			min = i;
			for (int j = i + step; j < left + size * step; j += step) {
				if (a.get(j).compareTo(a.get(min)) < 0) {
					min = j;
				}
			}
			Collections.swap(a, i, min);
		}
	}

	public T fastmedian(List<T> A) {

		// /////////////////////////////////////////
		// / Size of the array
		int size = A.size();

		// /////////////////////////////////////////
		// / Median calculation
		boolean LeftToRight = false;

		// Parameters
		int threshold = 2; // pass as argument !!!!!!!!!

		// Definitions
		int left = 0;
		int rem;
		int step = 1;
		int i, j;

		// / Run
		while (size > threshold) {
			LeftToRight = !LeftToRight;
			rem = size % 3;

			i = LeftToRight ? left : left + (3 + rem) * step;

			for (j = 0; j < (size / 3 - 1); j++) {
				adjustTriplet(A, i, step);
				i += 3 * step;
			}
			if (LeftToRight) {
				left += step;
			} else {
				i = left;
				left += (1 + rem) * step;
			}
			selectionSort(A, i, 3 + rem, step);
			if (rem == 2) {
				if (LeftToRight)
					Collections.swap(A, i + step, i + 2 * step);
				else
					Collections.swap(A, i + 2 * step, i + 3 * step);
			}
			step = 3 * step;
			size = size / 3;
		}
		selectionSort(A, left, size, step);
		T median = A.get(left + step * ((size - 1) / 2));

		// return median value
		return median;
	}

	public T select(final List<T> values, final int kin) {
		int k = kin;
		int left = 0;
		int right = values.size() - 1;
		Random rand = new Random();
		while (right >= left) {
			int partionIndex = rand.nextInt(right - left + 1) + left;
			int newIndex = partition(values, left, right, partionIndex);
			int q = newIndex - left + 1;
			if (k == q) {
				return values.get(newIndex);
			} else if (k < q) {
				right = newIndex - 1;
			} else {
				k -= q;
				left = newIndex + 1;
			}
		}
		return null;
	}

	private int partition(final List<T> values, final int left,	final int right, final int partitionIndex) {
		T partionValue = values.get(partitionIndex);
		int newIndex = left;
		T temp = values.get(partitionIndex);
		values.set(partitionIndex, values.get(right));
		values.set(right, temp);
		for (int i = left; i < right; i++) {
			if (values.get(i).compareTo(partionValue) < 0) {
				temp = values.get(i);
				values.set(i, values.get(newIndex));
				values.set(newIndex, temp);
				newIndex++;
			}
		}
		temp = values.get(right);
		values.set(right, values.get(newIndex));
		values.set(newIndex, temp);
		return newIndex;
	}

}
