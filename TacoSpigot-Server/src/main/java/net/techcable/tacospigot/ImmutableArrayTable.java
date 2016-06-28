package net.techcable.tacospigot;

import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;

import net.techcable.tacospigot.function.ObjIntBiFunction;

public class ImmutableArrayTable<R, C, V> {
  private final ImmutableArrayMap<R, ImmutableArrayMap<C, V>> rowMap;

  public ImmutableArrayTable(ToIntFunction<R> rowIndexer, IntFunction<R> rowById, ToIntBiFunction<R, C> columnGetId, ObjIntBiFunction<R, C> columnById, Table<R, C, V> table) {
      Preconditions.checkNotNull(table, "Null table");
      ImmutableMap.Builder<R, ImmutableArrayMap<C, V>> rowMapBuilder = ImmutableMap.builder();
      for (Map.Entry<R, Map<C, V>> rowEntry : table.rowMap().entrySet()) {
          R row = rowEntry.getKey();
          Preconditions.checkNotNull(row, "Null row");
          ImmutableMap.Builder<C, V> rowMapEntryBuilder = ImmutableMap.builder();
          for (Map.Entry<C, V> rowEntryEntry : rowEntry.getValue().entrySet()) {
              rowMapEntryBuilder.put(rowEntryEntry);
          }
          rowMapBuilder.put(row, new ImmutableArrayMap<>((c) -> columnGetId.applyAsInt(row, c), (id) -> columnById.apply(row, id), rowMapEntryBuilder.build()));
      }
      this.rowMap = new ImmutableArrayMap<>(rowIndexer, rowById, rowMapBuilder.build());
  }

  public V get(int rowId, int columnId) {
      ImmutableArrayMap<C, V> rowEntry = rowMap.get(rowId);
      return rowEntry != null ? rowEntry.get(columnId) : null;
  }
}