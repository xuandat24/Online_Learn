"""One-time scaffolding from the supplied SDD SQL; generated files are committed."""
from pathlib import Path
import re, shutil, os
root = Path(os.environ.get('OLS_GENERATE_ROOT', Path(__file__).resolve().parents[1]))
db = root / 'database'
db.mkdir(exist_ok=True)
source = db / 'OnlineLearn_Database.reference.sql'
if not source.exists():
    shutil.copyfile(r'D:\Downloads\DOWNLOAD ZALO\OnlineLearn_Database.sql', source)
sql = source.read_text(encoding='utf-8-sig')
java = root / 'backend/src/main/java/vn/onlinelearn'
for folder in ['entity', 'repository', 'service', 'security', 'dto', 'controller', 'integration', 'exception', 'config']:
    (java / folder).mkdir(parents=True, exist_ok=True)
migration = root / 'backend/src/main/resources/db/migration'
migration.mkdir(parents=True, exist_ok=True)
v1 = sql[sql.index('/* 3.1 Users */'):]
v1 = re.sub(r"PRINT 'OnlineLearnDB created: 20 tables.';", '', v1)
(migration / 'V1__sdd_schema.sql').write_text('SET ANSI_NULLS ON;\nSET QUOTED_IDENTIFIER ON;\nGO\n' + v1, encoding='utf-8')
names = {'users': 'User', 'auth_tokens': 'AuthToken', 'settings': 'Setting', 'subjects': 'Subject', 'subject_dimensions': 'SubjectDimension', 'price_packages': 'PricePackage', 'quizzes': 'Quiz', 'lessons': 'Lesson', 'registrations': 'Registration', 'course_access': 'CourseAccess', 'lesson_progress': 'LessonProgress', 'questions': 'Question', 'question_options': 'QuestionOption', 'quiz_attempts': 'QuizAttempt', 'quiz_answers': 'QuizAnswer', 'posts': 'Post', 'sliders': 'Slider', 'payment_transactions': 'PaymentTransaction'}
extra = {'User': [('int','tokenVersion','token_version','0')], 'Registration': [('String','publicTokenHash','public_token_hash',None), ('int','durationMonths','duration_months','1'), ('Long','createdBy','created_by',None)], 'PaymentTransaction': [('LocalDateTime','expiresAt','expires_at',None)]}
def camel(s):
    parts = s.split('_')
    return parts[0] + ''.join(x.title() for x in parts[1:])
for table, body in re.findall(r'CREATE TABLE dbo\.(\w+) \((.*?)\n\);', sql, re.S):
    if table not in names: continue
    name = names[table]
    fields = []
    for line in body.splitlines():
        m = re.match(r'\s+(\w+)\s+(BIGINT|INT|NVARCHAR|VARCHAR|CHAR|DATETIME2|BIT|DECIMAL)(?:\(([^)]*)\))?\s+(.*)', line)
        if not m: continue
        col, typ, length, tail = m.groups()
        jtype = {'BIGINT':'Long','INT':'Integer','NVARCHAR':'String','VARCHAR':'String','CHAR':'String','DATETIME2':'LocalDateTime','BIT':'boolean','DECIMAL':'BigDecimal'}[typ]
        ann = []
        if 'IDENTITY' in tail: ann.append('    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)')
        if col in ['password_hash','token_hash']: ann.append('    @JsonIgnore')
        opts = f'name = "{col}"'
        if typ == 'NVARCHAR': ann.append('    @org.hibernate.annotations.Nationalized')
        if length == 'MAX': opts += ', columnDefinition = "nvarchar(max)"'
        elif typ in ['VARCHAR','NVARCHAR','CHAR'] and length: opts += f', length = {length}'
        if typ == 'CHAR': opts += ', columnDefinition = "char(3)"'
        if typ == 'DECIMAL':
            precision, scale = length.split(',')
            opts += f', precision = {precision}, scale = {scale}'
        if 'NOT NULL' in tail: opts += ', nullable = false'
        ann.append(f'    @Column({opts})')
        default = ''
        dm = re.search(r"DEFAULT '([^']*)'", tail)
        if dm: default = f' = "{dm.group(1)}"'
        if 'DEFAULT SYSUTCDATETIME' in tail: default = ' = LocalDateTime.now(java.time.Clock.systemUTC())'
        ann.append(f'    public {jtype} {camel(col)}{default};')
        fields.append('\n'.join(ann))
    for typ, prop, col, val in extra.get(name, []):
        ignore = '    @JsonIgnore\n' if prop in ['publicTokenHash','tokenVersion'] else ''
        fields.append(f'{ignore}    @Column(name = "{col}")\n    public {typ} {prop}' + (f' = {val}' if val else '') + ';')
    if name == 'Quiz':
        fields.append('    @ElementCollection(fetch = FetchType.EAGER)\n    @CollectionTable(name = "quiz_questions", joinColumns = @JoinColumn(name = "quiz_id"))\n    @Column(name = "question_id")\n    @OrderColumn(name = "sort_order")\n    public java.util.List<Long> questionIds = new java.util.ArrayList<>();')
    if name == 'Question':
        fields.append('    @ElementCollection(fetch = FetchType.EAGER)\n    @CollectionTable(name = "question_dimensions", joinColumns = @JoinColumn(name = "question_id"))\n    @Column(name = "dimension_id")\n    public java.util.Set<Long> dimensionIds = new java.util.HashSet<>();')
    text = f'package vn.onlinelearn.entity;\n\nimport jakarta.persistence.*;\nimport java.time.LocalDateTime;\nimport java.math.BigDecimal;\nimport com.fasterxml.jackson.annotation.JsonIgnore;\n\n@Entity\n@Table(name = "{table}")\npublic class {name} {{\n' + '\n\n'.join(fields) + '\n}\n'
    (java / 'entity' / f'{name}.java').write_text(text, encoding='utf-8')
    methods = {
        'User': 'Optional<User> findByEmail(String email);\n    boolean existsByEmail(String email);',
        'AuthToken': '@Lock(LockModeType.PESSIMISTIC_WRITE)\n    Optional<AuthToken> findByTokenHash(String tokenHash);',
        'Subject': 'boolean existsByNameIgnoreCase(String name);',
        'PricePackage': 'List<PricePackage> findBySubjectId(Long subjectId);',
        'Lesson': 'List<Lesson> findBySubjectIdOrderBySortOrderAsc(Long subjectId);',
        'SubjectDimension': 'List<SubjectDimension> findBySubjectId(Long subjectId);',
        'Question': 'List<Question> findBySubjectId(Long subjectId);',
        'QuestionOption': 'List<QuestionOption> findByQuestionIdOrderBySortOrderAsc(Long questionId);\n    void deleteByQuestionId(Long questionId);',
        'Quiz': 'List<Quiz> findBySubjectId(Long subjectId);',
        'QuizAttempt': 'boolean existsByQuizId(Long quizId);\n    List<QuizAttempt> findByUserIdAndQuizIdOrderByStartedAtDesc(Long userId, Long quizId);\n    boolean existsByUserIdAndQuizIdAndStatus(Long userId, Long quizId, String status);',
        'QuizAnswer': 'List<QuizAnswer> findByAttemptId(Long attemptId);\n    Optional<QuizAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);',
        'Registration': 'List<Registration> findByCustomerUserIdOrderByRegisteredAtDesc(Long userId);\n    List<Registration> findByEmail(String email);',
        'PaymentTransaction': '@Lock(LockModeType.PESSIMISTIC_WRITE)\n    Optional<PaymentTransaction> findByOrderCode(String orderCode);\n    List<PaymentTransaction> findByRegistrationIdOrderByCreatedAtDesc(Long registrationId);\n    boolean existsByProviderTxnRef(String providerTxnRef);',
        'CourseAccess': 'List<CourseAccess> findByUserId(Long userId);\n    boolean existsByRegistrationId(Long registrationId);',
        'LessonProgress': 'List<LessonProgress> findByUserId(Long userId);\n    Optional<LessonProgress> findByUserIdAndLessonId(Long userId, Long lessonId);',
        'Setting': 'Optional<Setting> findByTypeAndValue(String type, String value);'
    }.get(name, '')
    pk = camel(re.search(r'(\w+)\s+BIGINT IDENTITY', body).group(1))
    methods += f'\n    @Lock(LockModeType.PESSIMISTIC_WRITE)\n    @Query("select e from {name} e where e.{pk} = :id")\n    Optional<{name}> lockById(@Param("id") Long id);'
    repo = f'package vn.onlinelearn.repository;\n\nimport vn.onlinelearn.entity.{name};\nimport org.springframework.data.jpa.repository.*;\nimport org.springframework.data.repository.query.Param;\nimport jakarta.persistence.LockModeType;\nimport java.util.*;\n\npublic interface {name}Repository extends JpaRepository<{name}, Long>, JpaSpecificationExecutor<{name}> {{\n    {methods}\n}}\n'
    (java / 'repository' / f'{name}Repository.java').write_text(repo, encoding='utf-8')
print('Generated 18 entities and repositories, 20-table non-destructive baseline migration.')
