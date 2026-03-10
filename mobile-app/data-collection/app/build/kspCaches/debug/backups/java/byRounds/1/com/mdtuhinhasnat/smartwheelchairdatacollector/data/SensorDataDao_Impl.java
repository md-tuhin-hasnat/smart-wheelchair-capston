package com.mdtuhinhasnat.smartwheelchairdatacollector.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SensorDataDao_Impl implements SensorDataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SessionEntity> __insertionAdapterOfSessionEntity;

  private final EntityInsertionAdapter<SensorReadingEntity> __insertionAdapterOfSensorReadingEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSessionById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllSessions;

  public SensorDataDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSessionEntity = new EntityInsertionAdapter<SessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sessions` (`sessionId`,`timestamp`) VALUES (nullif(?, 0),?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SessionEntity entity) {
        statement.bindLong(1, entity.getSessionId());
        statement.bindLong(2, entity.getTimestamp());
      }
    };
    this.__insertionAdapterOfSensorReadingEntity = new EntityInsertionAdapter<SensorReadingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `sensor_readings` (`id`,`sessionId`,`sampleIndex`,`ax`,`ay`,`az`,`gx`,`gy`,`gz`,`weight`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SensorReadingEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSessionId());
        statement.bindLong(3, entity.getSampleIndex());
        statement.bindDouble(4, entity.getAx());
        statement.bindDouble(5, entity.getAy());
        statement.bindDouble(6, entity.getAz());
        statement.bindDouble(7, entity.getGx());
        statement.bindDouble(8, entity.getGy());
        statement.bindDouble(9, entity.getGz());
        statement.bindDouble(10, entity.getWeight());
      }
    };
    this.__preparedStmtOfDeleteSessionById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions WHERE sessionId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllSessions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM sessions";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final SessionEntity session,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSessionEntity.insertAndReturnId(session);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertReadings(final List<SensorReadingEntity> readings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSensorReadingEntity.insert(readings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSessionWithReadings(final SessionEntity session,
      final List<SensorReadingEntity> readings, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> SensorDataDao.DefaultImpls.insertSessionWithReadings(SensorDataDao_Impl.this, session, readings, __cont), $completion);
  }

  @Override
  public Object deleteSessionById(final long sessionId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSessionById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sessionId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSessionById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllSessions(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllSessions.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllSessions.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SessionWithReadings>> getAllSessionsWithReadings() {
    final String _sql = "SELECT * FROM sessions ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"sensor_readings",
        "sessions"}, new Callable<List<SessionWithReadings>>() {
      @Override
      @NonNull
      public List<SessionWithReadings> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
            final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
            final LongSparseArray<ArrayList<SensorReadingEntity>> _collectionReadings = new LongSparseArray<ArrayList<SensorReadingEntity>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfSessionId);
              if (!_collectionReadings.containsKey(_tmpKey)) {
                _collectionReadings.put(_tmpKey, new ArrayList<SensorReadingEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipsensorReadingsAscomMdtuhinhasnatSmartwheelchairdatacollectorDataSensorReadingEntity(_collectionReadings);
            final List<SessionWithReadings> _result = new ArrayList<SessionWithReadings>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final SessionWithReadings _item;
              final SessionEntity _tmpSession;
              final long _tmpSessionId;
              _tmpSessionId = _cursor.getLong(_cursorIndexOfSessionId);
              final long _tmpTimestamp;
              _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
              _tmpSession = new SessionEntity(_tmpSessionId,_tmpTimestamp);
              final ArrayList<SensorReadingEntity> _tmpReadingsCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfSessionId);
              _tmpReadingsCollection = _collectionReadings.get(_tmpKey_1);
              _item = new SessionWithReadings(_tmpSession,_tmpReadingsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipsensorReadingsAscomMdtuhinhasnatSmartwheelchairdatacollectorDataSensorReadingEntity(
      @NonNull final LongSparseArray<ArrayList<SensorReadingEntity>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipsensorReadingsAscomMdtuhinhasnatSmartwheelchairdatacollectorDataSensorReadingEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`sessionId`,`sampleIndex`,`ax`,`ay`,`az`,`gx`,`gy`,`gz`,`weight` FROM `sensor_readings` WHERE `sessionId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "sessionId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfSessionId = 1;
      final int _cursorIndexOfSampleIndex = 2;
      final int _cursorIndexOfAx = 3;
      final int _cursorIndexOfAy = 4;
      final int _cursorIndexOfAz = 5;
      final int _cursorIndexOfGx = 6;
      final int _cursorIndexOfGy = 7;
      final int _cursorIndexOfGz = 8;
      final int _cursorIndexOfWeight = 9;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<SensorReadingEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final SensorReadingEntity _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final long _tmpSessionId;
          _tmpSessionId = _cursor.getLong(_cursorIndexOfSessionId);
          final int _tmpSampleIndex;
          _tmpSampleIndex = _cursor.getInt(_cursorIndexOfSampleIndex);
          final float _tmpAx;
          _tmpAx = _cursor.getFloat(_cursorIndexOfAx);
          final float _tmpAy;
          _tmpAy = _cursor.getFloat(_cursorIndexOfAy);
          final float _tmpAz;
          _tmpAz = _cursor.getFloat(_cursorIndexOfAz);
          final float _tmpGx;
          _tmpGx = _cursor.getFloat(_cursorIndexOfGx);
          final float _tmpGy;
          _tmpGy = _cursor.getFloat(_cursorIndexOfGy);
          final float _tmpGz;
          _tmpGz = _cursor.getFloat(_cursorIndexOfGz);
          final float _tmpWeight;
          _tmpWeight = _cursor.getFloat(_cursorIndexOfWeight);
          _item_1 = new SensorReadingEntity(_tmpId,_tmpSessionId,_tmpSampleIndex,_tmpAx,_tmpAy,_tmpAz,_tmpGx,_tmpGy,_tmpGz,_tmpWeight);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
