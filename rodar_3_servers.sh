java -cp bin:lib/sqlite-jdbc-3.27.2.1.jar:lib/commons-cli-1.3.1.jar app.server.Server -ref_remota server1 &
sleep 2s
java -cp bin:lib/sqlite-jdbc-3.27.2.1.jar:lib/commons-cli-1.3.1.jar app.server.Server -ref_remota server2 &
sleep 2s
java -cp bin:lib/sqlite-jdbc-3.27.2.1.jar:lib/commons-cli-1.3.1.jar app.server.Server -ref_remota server3 &
sleep 2s
