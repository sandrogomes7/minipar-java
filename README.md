### 📄 **README - MiniParJava**

Este é o projeto **MiniParJava**, um interpretador em Java para a linguagem MiniPar.  
Da disciplina de Compiladores 2024.2, ministrada pelo professor **Arturo Hernández Domínguez**.  
O projeto utiliza **Java 21**, **Maven** para gerenciamento de dependências e build, e **Makefile** para facilitar a execução.

Equipe:
- Erick Keven da Silva Alves
- Gabriel Lisboa Conde da Rocha
- Marcos Douglas de Santana Ferreira
- Paulo Laercio de Oliveira Junior
- Sandro Gomes Paulino

---

### 🚀 **Pré-requisitos**

Antes de executar o projeto, verifique se você tem as seguintes ferramentas instaladas:

* **Java 21 (JDK 21)**
* **Maven**
* **Make**

---

### ⚙️ **Como Rodar o Projeto**

1. **Clone o repositório**:

   Se você ainda não clonou o repositório, use o comando:

   ```bash
   git clone <url-do-repositorio>
   cd <diretorio-do-repositorio>
   ```

2. **Compile e Execute o Projeto**:

   Para compilar e rodar o interpretador, utilize o comando:

   ```bash
   make run f=testes/nomedoarquivo.minipar
   ```

   O `Makefile` irá:

    * Verificar se o `.jar` do projeto já foi gerado.
    * Caso não exista ou esteja desatualizado, executará automaticamente `mvn clean package` para compilar o projeto.
    * Em seguida, executará o interpretador passando o arquivo `.minipar` especificado.

   **Exemplo**:

   ```bash
   make run f=testes/6-quicksort.minipar
   ```


3. **Resultado Esperado**:

   A saída do programa interpretado será exibida no terminal.

---

### 📂 **Estrutura do Projeto**

* **src/**: Código-fonte Java do interpretador.
* **testes/**: Arquivos de entrada `.minipar` para testes.
* **target/**: Saída da compilação Maven (incluindo o `.jar` do projeto).
