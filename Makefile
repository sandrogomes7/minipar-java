# Nome do arquivo .jar gerado após a compilação
JAR_FILE = target/MiniParJava-1.0-SNAPSHOT.jar

# Diretório onde estão os arquivos .java
SRC_DIR = src/main/java

# Lista de todos os arquivos fonte
SOURCES := $(shell find $(SRC_DIR) -name "*.java")

# Regra padrão
all: run

# Regra de build: recompila se os arquivos .java forem modificados
$(JAR_FILE): $(SOURCES)
	mvn clean package

# Comando para rodar o interpretador
run: $(JAR_FILE)
	clear
	java -jar $(JAR_FILE) $(f)
