# ConsulThi - Sistema de Gestão de Consultoria Fitness

Este repositório contém o código-fonte e a documentação do sistema **ConsulThi**, desenvolvido como trabalho final da disciplina **Análise e Projeto de Software Orientado a Objetos**, ministrada no semestre **2025/2** pela **Universidade Federal de Mato Grosso do Sul (UFMS)**.

O projeto consiste em uma aplicação híbrida (Web e Desktop) para gerenciamento de consultorias de treino e nutrição, conectando profissionais (Treinadores, Nutricionistas, Psicólogos) a alunos.

## 📋 Sobre o Projeto

O **ConsulThi** foi projetado para resolver a dificuldade de personalização e acompanhamento em consultorias fitness. O sistema permite a criação e gestão de treinos, dietas e materiais de apoio, além de controlar o acesso e vínculos entre profissionais e alunos.

### Escopo da Solução

O trabalho exigia o desenvolvimento de um componente Desktop. A arquitetura adotada foi:

1.  **Aplicação Web (Core):** Onde alunos e profissionais interagem (dashboards, visualização de conteúdo, registros).
2.  **Módulo Desktop (Administrativo):** Uma aplicação JavaFX integrada ao ecossistema Spring, exclusiva para Administradores, focada na aprovação de cadastros profissionais e gestão de usuários.

## 🚀 Tecnologias Utilizadas

O projeto utiliza uma stack moderna baseada em **Java 23**:

  * **Backend:** Java 23, Spring Boot 3.5.7 (Web, Security, Data JPA).
  * **Frontend (Web):** Thymeleaf, HTMX, Tailwind CSS (via Node.js).
  * **Frontend (Desktop):** JavaFX 23.
  * **Banco de Dados:** PostgreSQL 16 (via Docker).
  * **Build & Dependências:** Maven.

## ⚙️ Pré-requisitos

Para executar o projeto, certifique-se de ter instalado:

  * **Java JDK 23**
  * **Docker & Docker Compose** (Para o banco de dados)
  * **Node.js 20+ & NPM** (Para compilação do Tailwind CSS)

## 🛠️ Como Executar

### 1\. Clonar o Repositório

```bash
git clone https://github.com/seu-usuario/consulthi.git
cd consulthi/Project
```

### 2\. Subir o Banco de Dados

Utilize o Docker Compose para iniciar o PostgreSQL e o pgAdmin (opcional):

```bash
docker-compose up -d
```

*Isso iniciará o banco na porta `5433` com as credenciais configuradas em `application.yml`.*

### 3\. Compilar os Estilos (Tailwind CSS)

O projeto utiliza um script para instalar dependências e gerar o CSS:

```bash
# Instala dependências do Node
npm install

# Gera o arquivo output.css inicial
npm run build:css
```

### 4\. Executar a Aplicação

Para rodar o sistema completo (Servidor Web + Interface Desktop Admin):

**Via Script (Linux/Mac):**

```bash
chmod +x start-dev.sh
./start-dev.sh
```

**Via Maven (Windows/Linux/Mac):**

```bash
./mvnw spring-boot:run
```

*Observação: A aplicação Desktop (JavaFX) será iniciada automaticamente junto com o servidor Spring Boot, pois a classe `AdminDesktopApplication` inicializa o contexto do Spring.*

## 🖥️ Acesso ao Sistema

Após a inicialização:

  * **Web (Alunos e Profissionais):** Acesse `http://localhost:8080`
  * **Desktop (Administradores):** A janela abrirá automaticamente no servidor.
  * **PgAdmin:** Acesse `http://localhost:8081` (Email: `admin@consulthi.com`, Senha: `admin`)

### Credenciais de Teste (Geradas pelo `DataInitializer`)

O sistema inicializa com dados padrão para facilitar a correção:

  * **Admin:** `admin1` / `123456`
  * **Profissional (Coach):** `coach1` / `123456`
  * **Aluno:** `student` / `123456`

## 📂 Estrutura do Projeto

  * `src/main/java/.../controller`: Controladores Web MVC.
  * `src/main/java/.../desktop`: Controladores e Cliente REST da aplicação JavaFX.
  * `src/main/java/.../model`: Entidades JPA (uso de herança em `Content`).
  * `src/main/java/.../service`: Regras de negócio e Agendador (Scheduler).
  * `src/main/resources/templates`: Views do Thymeleaf.
  * `src/main/resources/desktop`: Arquivos `.fxml` das telas Desktop.

## 📚 Documentação

O **Manual do Sistema** completo, incluindo Diagramas de Classe, Casos de Uso e Requisitos (RF/RNF), encontra-se na raiz do repositório no arquivo `Manual Sistema.pdf`.


## 👥 Autores

| [<img loading="lazy" src="https://avatars.githubusercontent.com/u/68046889?v=4" width=115><br><sub>Arthur de Andrade</sub>](https://github.com/shiro-sama404) |  [<img loading="lazy" src="https://avatars.githubusercontent.com/u/131722952?v=4" width=115><br><sub>Felipe Jun</sub>](https://github.com/FelipeTakahashi) |  [<img loading="lazy" src="https://avatars.githubusercontent.com/u/105750957?v=4" width=115><br><sub>Rodrigo Kenji</sub>](https://github.com/rkenjiak) |
| :---: | :---: | :---: |

-----

**UFMS - Faculdade de Computação (FACOM)**
*Trabalho avaliativo da disciplina de Análise e Projeto de Software Orientado a Objetos.*
