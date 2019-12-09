dir /s /B *.java > sources.txt
javac -cp lib\sqlite-jdbc-3.27.2.1.jar;lib\commons-cli-1.3.1.jar -d bin @sources.txt