# Nome do arquivo .jar gerado após a compilação
JAR_FILE=target/MiniParJava-1.0-SNAPSHOT.jar

# Comando para compilar o projeto
build:
	mvn clean package

# Comando para rodar o interpretador
run: $(JAR_FILE)
	clear
	java -jar $(JAR_FILE) $(f)

# Se o .jar não estiver presente, o Makefile vai rodar o build automaticamente antes de rodar o interpretador
$(JAR_FILE):
	mvn clean package
