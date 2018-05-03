package com.lapissea.datamanager.domains.db;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.SQLException;

public interface FireOnlyTrigger extends Trigger{
	@Override
	default void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException{ }
	
	@Override
	default void close() throws SQLException{ }
	
	@Override
	default void remove() throws SQLException{ }
}
