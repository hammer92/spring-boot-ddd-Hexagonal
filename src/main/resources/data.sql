INSERT INTO chapter (id, name) VALUES ('backend', 'Backend');
INSERT INTO chapter (id, name) VALUES ('frontend','Frontend');
INSERT INTO chapter (id, name) VALUES ('qa','QA');
INSERT INTO chapter (id, name) VALUES ('devops', 'DevOps');


INSERT INTO users (id, first_name, last_name,correo, google_user_id, chapter_id, rol, active_tutoring_limit) VALUES ('tutee-1', 'Jamer', 'Tutee', 'tutee-1@pragma.com', 'google-tutee-1', 'backend', 'Tutorado', 0 );
INSERT INTO users (id, first_name, last_name,correo, google_user_id, chapter_id, rol, active_tutoring_limit) VALUES ('tutee-2', 'Carlos', 'Tutee', 'tutee-2@pragma.com', 'google-tutee-2', 'backend', 'Tutorado', 0 );
INSERT INTO users (id, first_name, last_name,correo, google_user_id, chapter_id, rol, active_tutoring_limit) VALUES ('tutee-3', 'David', 'Tutee', 'tutee-3@pragma.com', 'google-tutee-3', 'backend', 'Tutorado', 0 );
INSERT INTO users (id, first_name, last_name,correo, google_user_id, chapter_id, rol, active_tutoring_limit) VALUES ('tutor-1', 'Juan', 'Tutor', 'tutor-1@pragma.com', 'google-tutor-1', 'backend', 'Tutor', 1 );
INSERT INTO users (id, first_name, last_name,correo, google_user_id, chapter_id, rol, active_tutoring_limit) VALUES ('tutor-2', 'Marcos', 'Tutor', 'tutor-2@pragma.com', 'google-tutor-2', 'backend', 'Tutor', 2 );
INSERT INTO users (id, first_name, last_name,correo, google_user_id, chapter_id, rol, active_tutoring_limit) VALUES ('tutor-3', 'Maria', 'Tutor', 'tutor-3@pragma.com', 'google-tutor-3', 'backend', 'Tutor', 3 );
INSERT INTO users (id, first_name, last_name,correo, google_user_id, chapter_id, rol, active_tutoring_limit) VALUES ('admin', 'Admin', 'System', 'admin@pragma.com', 'google-admin', 'backend', 'Administrador', 3 );



INSERT INTO skills (id, name) VALUES ('skills-1', 'Serverless Framework');
INSERT INTO skills (id, name) VALUES ('skills-2','Nest');
INSERT INTO skills (id, name) VALUES ('skills-3','Express');
INSERT INTO skills (id, name) VALUES ('skills-4', 'Spring Boot');
INSERT INTO skills (id, name) VALUES ('skills-5', 'Quarkus');
INSERT INTO skills (id, name) VALUES ('skills-6', 'ORM');
INSERT INTO skills (id, name) VALUES ('skills-7', 'ODM');
INSERT INTO skills (id, name) VALUES ('skills-8', 'Programación Orientada a Objetos');
INSERT INTO skills (id, name) VALUES ('skills-9', 'Programación Funcional');
INSERT INTO skills (id, name) VALUES ('skills-10', 'Programación Reactiva');
INSERT INTO skills (id, name) VALUES ('skills-11', 'Programación Orientada a Aspectos');