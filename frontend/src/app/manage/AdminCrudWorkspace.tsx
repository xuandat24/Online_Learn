"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import "../workspace.css";

type Role = "ADMIN" | "SALE" | "MARKETING" | "EXPERT" | "CUSTOMER";
type Account = {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  gender: string;
  role: Role;
  status: "ACTIVE" | "LOCKED";
  createdAt: string;
};
type Subject = { id: number; name: string; description: string; active: boolean };
type Dimension = {
  id: number;
  subjectId: number;
  name: string;
  description: string;
  displayOrder: number;
  active: boolean;
};
type PricePackage = {
  id?: number;
  name: string;
  price: number;
  currency: string;
  accessDays: number;
  published: boolean;
};
type Course = {
  id: number;
  title: string;
  description: string;
  category: string;
  instructor: string;
  subjectId: number;
  subjectName: string;
  level: string;
  duration: string;
  price: number;
  image: string;
  accent: string;
  published: boolean;
  pricePackages: PricePackage[];
};
type CourseDraft = Omit<Course, "id" | "subjectName"> & { id?: number };
type Lesson = {
  id: number;
  courseId: number;
  title: string;
  summary: string;
  content: string;
  videoUrl: string | null;
  displayOrder: number;
  published: boolean;
  completed: boolean;
};
type Question = {
  id: number;
  subject: string;
  lessonTitle: string;
  level: "EASY" | "MEDIUM" | "HARD";
  prompt: string;
  optionsJson: string;
  correctOptionIndex: number;
  explanation: string;
  status: "ACTIVE" | "INACTIVE";
};
type Setting = {
  id: number;
  settingGroup: string;
  name: string;
  value: string;
  displayOrder: number;
  active: boolean;
};
type Tab = "accounts" | "subjects" | "lessons" | "questions" | "settings";

const roles: Role[] = ["ADMIN", "SALE", "MARKETING", "EXPERT", "CUSTOMER"];
const emptySubject = { name: "", description: "", active: true };
const emptyDimension = { name: "", description: "", displayOrder: 1, active: true };
const emptySetting = { settingGroup: "", name: "", value: "", displayOrder: 1, active: true };
const emptyLesson = {
  title: "",
  summary: "",
  content: "",
  videoUrl: "",
  displayOrder: 1,
  published: false,
};
const emptyQuestion = {
  subject: "",
  lessonTitle: "",
  level: "MEDIUM" as Question["level"],
  prompt: "",
  options: ["", "", "", ""],
  correctOptionIndex: 0,
  explanation: "",
  status: "ACTIVE" as Question["status"],
};
const emptyCourse = {
  title: "",
  description: "",
  category: "",
  instructor: "",
  subjectId: 0,
  level: "Beginner",
  duration: "",
  price: 0,
  image: "",
  accent: "",
  published: false,
  pricePackages: [{ name: "", price: 0, currency: "USD", accessDays: 30, published: true }],
};

async function api<T>(url: string, init: RequestInit = {}): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: {
      ...(init.body ? { "Content-Type": "application/json" } : {}),
      ...init.headers,
    },
  });
  if (!response.ok) {
    const body = await response.text();
    let message = body || response.statusText;
    try {
      const parsed = JSON.parse(body) as { message?: string };
      message = parsed.message || message;
    } catch {
      // Non-JSON error responses are shown as returned by the server.
    }
    throw new Error(`${response.status}: ${message}`);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

function parseOptions(value: string): string[] {
  const parsed: unknown = JSON.parse(value);
  if (!Array.isArray(parsed) || !parsed.every((item) => typeof item === "string")) {
    throw new Error("Dữ liệu đáp án của câu hỏi không hợp lệ.");
  }
  return parsed;
}

export default function AdminCrudWorkspace() {
  const [tab, setTab] = useState<Tab>("accounts");
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [dimensions, setDimensions] = useState<Dimension[]>([]);
  const [courses, setCourses] = useState<Course[]>([]);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [settings, setSettings] = useState<Setting[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [questionSearch, setQuestionSearch] = useState("");
  const [questionLevel, setQuestionLevel] = useState("ALL");
  const [questionStatus, setQuestionStatus] = useState("ALL");
  const [selectedSubjectId, setSelectedSubjectId] = useState<number | null>(null);
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  const [accountDraft, setAccountDraft] = useState<{
    id?: number;
    fullName: string;
    email: string;
    password: string;
    phone: string;
    gender: string;
    role: Role;
    status: "ACTIVE" | "LOCKED";
  } | null>(null);
  const [subjectDraft, setSubjectDraft] = useState<{ id?: number } & typeof emptySubject | null>(null);
  const [dimensionDraft, setDimensionDraft] = useState<{ id?: number } & typeof emptyDimension | null>(null);
  const [settingDraft, setSettingDraft] = useState<{ id?: number } & typeof emptySetting | null>(null);
  const [lessonDraft, setLessonDraft] = useState<{ id?: number } & typeof emptyLesson | null>(null);
  const [questionDraft, setQuestionDraft] = useState<{ id?: number } & typeof emptyQuestion | null>(null);
  const [courseDraft, setCourseDraft] = useState<CourseDraft | null>(null);

  const loadData = useCallback(async () => {
    try {
      const [nextAccounts, nextSubjects, nextCourses, nextQuestions, nextSettings] = await Promise.all([
        api<Account[]>("/api/admin/users"),
        api<Subject[]>("/api/admin/subjects"),
        api<Course[]>("/api/admin/courses"),
        api<Question[]>("/api/admin/questions"),
        api<Setting[]>("/api/admin/settings"),
      ]);
      setAccounts(nextAccounts);
      setSubjects(nextSubjects);
      setCourses(nextCourses);
      setQuestions(nextQuestions);
      setSettings(nextSettings);
      setSelectedSubjectId((current) => current ?? nextSubjects[0]?.id ?? null);
      setSelectedCourseId((current) => current ?? nextCourses[0]?.id ?? null);
      setError("");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadData().catch((reason: unknown) => {
      setError(reason instanceof Error ? reason.message : "Không thể tải dữ liệu quản trị.");
    });
  }, [loadData]);

  useEffect(() => {
    if (selectedSubjectId === null) return;
    let current = true;
    void api<Dimension[]>(`/api/admin/subjects/${selectedSubjectId}/dimensions`)
      .then((items) => { if (current) setDimensions(items); })
      .catch((reason: unknown) => {
        if (current) setError(reason instanceof Error ? reason.message : "Không tải được chiều môn học.");
      });
    return () => { current = false; };
  }, [selectedSubjectId]);

  useEffect(() => {
    if (selectedCourseId === null) return;
    let current = true;
    void api<Lesson[]>(`/api/admin/courses/${selectedCourseId}/lessons`)
      .then((items) => { if (current) setLessons(items); })
      .catch((reason: unknown) => {
        if (current) setError(reason instanceof Error ? reason.message : "Không tải được bài học.");
      });
    return () => { current = false; };
  }, [selectedCourseId]);

  async function perform(action: () => Promise<void>) {
    setError("");
    setNotice("");
    setBusy(true);
    try {
      await action();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Thao tác không thành công.");
    } finally {
      setBusy(false);
    }
  }

  const selectedSubject = subjects.find((subject) => subject.id === selectedSubjectId) ?? null;
  const subjectCourses = courses.filter((course) => course.subjectId === selectedSubjectId);
  const filteredAccounts = useMemo(() => accounts.filter((account) => {
    const query = search.trim().toLowerCase();
    return (roleFilter === "ALL" || account.role === roleFilter)
      && (!query || `${account.fullName} ${account.email} ${account.phone}`.toLowerCase().includes(query));
  }), [accounts, roleFilter, search]);
  const filteredQuestions = useMemo(() => questions.filter((question) => {
    const query = questionSearch.trim().toLowerCase();
    return (questionLevel === "ALL" || question.level === questionLevel)
      && (questionStatus === "ALL" || question.status === questionStatus)
      && (!query || `${question.subject} ${question.lessonTitle} ${question.prompt}`.toLowerCase().includes(query));
  }), [questionLevel, questionSearch, questionStatus, questions]);

  function startNewAccount() {
    setAccountDraft({
      fullName: "",
      email: "",
      password: "",
      phone: "",
      gender: "",
      role: "CUSTOMER",
      status: "ACTIVE",
    });
  }

  function editAccount(account: Account) {
    setAccountDraft({ ...account, password: "" });
  }

  async function saveAccount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!accountDraft) return;
    await perform(async () => {
      const body = accountDraft.id
        ? {
          fullName: accountDraft.fullName,
          password: accountDraft.password || undefined,
          phone: accountDraft.phone,
          gender: accountDraft.gender,
          role: accountDraft.role,
          status: accountDraft.status,
        }
        : {
          fullName: accountDraft.fullName,
          email: accountDraft.email,
          password: accountDraft.password,
          phone: accountDraft.phone,
          gender: accountDraft.gender,
          role: accountDraft.role,
        };
      await api<Account>(accountDraft.id ? `/api/admin/users/${accountDraft.id}` : "/api/admin/users", {
        method: accountDraft.id ? "PUT" : "POST",
        body: JSON.stringify(body),
      });
      setAccountDraft(null);
      await loadData();
      setNotice("Đã lưu tài khoản.");
    });
  }

  function startNewSubject() {
    setSubjectDraft({ ...emptySubject });
  }

  async function saveSubject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!subjectDraft) return;
    await perform(async () => {
      const saved = await api<Subject>(
        subjectDraft.id ? `/api/admin/subjects/${subjectDraft.id}` : "/api/admin/subjects",
        {
          method: subjectDraft.id ? "PUT" : "POST",
          body: JSON.stringify({
            name: subjectDraft.name,
            description: subjectDraft.description,
            active: subjectDraft.active,
          }),
        },
      );
      setSubjectDraft(null);
      await loadData();
      setSelectedSubjectId(saved.id);
      setNotice("Đã lưu môn học.");
    });
  }

  function startNewDimension() {
    setDimensionDraft({ ...emptyDimension });
  }

  async function saveDimension(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!dimensionDraft || selectedSubjectId === null) return;
    await perform(async () => {
      await api<Dimension>(
        dimensionDraft.id
          ? `/api/admin/subjects/${selectedSubjectId}/dimensions/${dimensionDraft.id}`
          : `/api/admin/subjects/${selectedSubjectId}/dimensions`,
        {
          method: dimensionDraft.id ? "PUT" : "POST",
          body: JSON.stringify(dimensionDraft),
        },
      );
      setDimensionDraft(null);
      setDimensions(await api<Dimension[]>(`/api/admin/subjects/${selectedSubjectId}/dimensions`));
      setNotice("Đã lưu chiều môn học.");
    });
  }

  function startNewCourse() {
    if (selectedSubjectId === null) return;
    setCourseDraft({ ...emptyCourse, subjectId: selectedSubjectId, pricePackages: [{ ...emptyCourse.pricePackages[0] }] });
  }

  function editCourse(course: Course) {
    setCourseDraft({
      id: course.id,
      title: course.title,
      description: course.description,
      category: course.category,
      instructor: course.instructor,
      subjectId: course.subjectId,
      level: course.level,
      duration: course.duration,
      price: course.price,
      image: course.image ?? "",
      accent: course.accent ?? "",
      published: course.published,
      pricePackages: course.pricePackages.map((pricePackage) => ({ ...pricePackage })),
    });
  }

  async function saveCourse(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!courseDraft || courseDraft.pricePackages.length === 0) return;
    await perform(async () => {
      await api<Course>(courseDraft.id ? `/api/admin/courses/${courseDraft.id}` : "/api/admin/courses", {
        method: courseDraft.id ? "PUT" : "POST",
        body: JSON.stringify(courseDraft),
      });
      setCourseDraft(null);
      await loadData();
      setNotice("Đã lưu khóa học và các gói giá.");
    });
  }

  function startNewLesson() {
    setLessonDraft({ ...emptyLesson });
  }

  async function saveLesson(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!lessonDraft || selectedCourseId === null) return;
    await perform(async () => {
      await api<Lesson>(
        lessonDraft.id
          ? `/api/admin/courses/${selectedCourseId}/lessons/${lessonDraft.id}`
          : `/api/admin/courses/${selectedCourseId}/lessons`,
        {
          method: lessonDraft.id ? "PUT" : "POST",
          body: JSON.stringify({ ...lessonDraft, videoUrl: lessonDraft.videoUrl || null }),
        },
      );
      setLessonDraft(null);
      setLessons(await api<Lesson[]>(`/api/admin/courses/${selectedCourseId}/lessons`));
      setNotice("Đã lưu bài học.");
    });
  }

  function editQuestion(question: Question) {
    try {
      setQuestionDraft({
        id: question.id,
        subject: question.subject,
        lessonTitle: question.lessonTitle || "",
        level: question.level,
        prompt: question.prompt,
        options: parseOptions(question.optionsJson),
        correctOptionIndex: question.correctOptionIndex,
        explanation: question.explanation || "",
        status: question.status,
      });
      setError("");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Không đọc được dữ liệu câu hỏi.");
    }
  }

  async function saveQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!questionDraft) return;
    await perform(async () => {
      const body = {
        subject: questionDraft.subject,
        lessonTitle: questionDraft.lessonTitle,
        level: questionDraft.level,
        prompt: questionDraft.prompt,
        optionsJson: JSON.stringify(questionDraft.options.map((option) => option.trim()).filter(Boolean)),
        correctOptionIndex: questionDraft.correctOptionIndex,
        explanation: questionDraft.explanation,
        status: questionDraft.status,
      };
      await api<Question>(
        questionDraft.id ? `/api/admin/questions/${questionDraft.id}` : "/api/admin/questions",
        { method: questionDraft.id ? "PUT" : "POST", body: JSON.stringify(body) },
      );
      setQuestionDraft(null);
      await loadData();
      setNotice("Đã lưu câu hỏi.");
    });
  }

  function startNewSetting() {
    setSettingDraft({ ...emptySetting });
  }

  async function saveSetting(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!settingDraft) return;
    await perform(async () => {
      await api<Setting>(
        settingDraft.id ? `/api/admin/settings/${settingDraft.id}` : "/api/admin/settings",
        {
          method: settingDraft.id ? "PUT" : "POST",
          body: JSON.stringify(settingDraft),
        },
      );
      setSettingDraft(null);
      await loadData();
      setNotice("Đã lưu cài đặt.");
    });
  }

  async function toggleAccountStatus(account: Account) {
    await perform(async () => {
      await api<Account>(`/api/admin/users/${account.id}`, {
        method: "PUT",
        body: JSON.stringify({
          fullName: account.fullName,
          phone: account.phone,
          gender: account.gender,
          role: account.role,
          status: account.status === "ACTIVE" ? "LOCKED" : "ACTIVE",
        }),
      });
      await loadData();
      setNotice("Đã đổi trạng thái tài khoản.");
    });
  }

  async function toggleSubject(subject: Subject) {
    await perform(async () => {
      await api<Subject>(`/api/admin/subjects/${subject.id}`, {
        method: "PUT",
        body: JSON.stringify({ ...subject, active: !subject.active }),
      });
      await loadData();
      setNotice("Đã đổi trạng thái môn học.");
    });
  }

  async function toggleSetting(setting: Setting) {
    await perform(async () => {
      await api<Setting>(`/api/admin/settings/${setting.id}`, {
        method: "PUT",
        body: JSON.stringify({ ...setting, active: !setting.active }),
      });
      await loadData();
      setNotice("Đã đổi trạng thái cài đặt.");
    });
  }

  async function toggleLesson(lesson: Lesson) {
    if (selectedCourseId === null) return;
    await perform(async () => {
      await api<Lesson>(`/api/admin/courses/${selectedCourseId}/lessons/${lesson.id}`, {
        method: "PUT",
        body: JSON.stringify({
          title: lesson.title,
          summary: lesson.summary,
          content: lesson.content,
          videoUrl: lesson.videoUrl,
          displayOrder: lesson.displayOrder,
          published: !lesson.published,
        }),
      });
      setLessons(await api<Lesson[]>(`/api/admin/courses/${selectedCourseId}/lessons`));
      setNotice("Đã đổi trạng thái bài học.");
    });
  }

  async function toggleQuestion(question: Question) {
    await perform(async () => {
      await api<Question>(`/api/admin/questions/${question.id}`, {
        method: "PUT",
        body: JSON.stringify({
          ...question,
          status: question.status === "ACTIVE" ? "INACTIVE" : "ACTIVE",
        }),
      });
      await loadData();
      setNotice("Đã đổi trạng thái câu hỏi.");
    });
  }

  async function deleteQuestion(question: Question) {
    if (!window.confirm(`Xóa câu hỏi "${question.prompt}"?`)) return;
    await perform(async () => {
      await api<void>(`/api/admin/questions/${question.id}`, { method: "DELETE" });
      await loadData();
      setNotice("Đã xóa câu hỏi.");
    });
  }

  async function deleteDimension(dimension: Dimension) {
    if (selectedSubjectId === null || !window.confirm(`Xóa chiều "${dimension.name}"?`)) return;
    await perform(async () => {
      await api<void>(`/api/admin/subjects/${selectedSubjectId}/dimensions/${dimension.id}`, { method: "DELETE" });
      setDimensions(await api<Dimension[]>(`/api/admin/subjects/${selectedSubjectId}/dimensions`));
      setNotice("Đã xóa chiều môn học.");
    });
  }

  const tabLabels: { id: Tab; label: string; count: number }[] = [
    { id: "accounts", label: "Tài khoản", count: accounts.length },
    { id: "subjects", label: "Môn học & gói giá", count: subjects.length },
    { id: "lessons", label: "Bài học", count: lessons.length },
    { id: "questions", label: "Ngân hàng câu hỏi", count: questions.length },
    { id: "settings", label: "Cài đặt", count: settings.length },
  ];

  return (
    <div className="workspace-page">
      <header className="workspace-header">
        <span className="brand">OnlineLearn</span>
        <div className="workspace-identity"><span>Quản trị dữ liệu hệ thống</span><strong>ADMIN</strong></div>
      </header>
      <main className="workspace-main">
        <div className="workspace-heading">
          <div>
            <div className="eyebrow"><span className="eyebrow-line" /><span>Quản trị hệ thống</span></div>
            <h1>Quản lý dữ liệu</h1>
          </div>
          <button className="button button-dark button-small" disabled={loading || busy}
            onClick={() => void perform(loadData)}>Làm mới dữ liệu</button>
        </div>
        <nav className="workspace-tabs" aria-label="Phân hệ quản trị">
          {tabLabels.map((item) => (
            <button key={item.id} className={tab === item.id ? "active" : ""} onClick={() => setTab(item.id)}>
              {item.label}<span className="tab-count">{item.count}</span>
            </button>
          ))}
        </nav>
        {error && <div className="workspace-message" role="alert"><span>Lỗi: {error}</span><button onClick={() => setError("")}>×</button></div>}
        {notice && <div className="workspace-message" role="status"><span>{notice}</span><button onClick={() => setNotice("")}>×</button></div>}
        {loading && <div className="workspace-panel">Đang tải dữ liệu từ máy chủ…</div>}
        {!loading && (
          <>
            {tab === "accounts" && (
              <section className="workspace-panel content-panel">
                <div className="panel-heading">
                  <h2>Tài khoản</h2>
                  <button className="button button-dark button-small" onClick={startNewAccount}>+ Tạo tài khoản</button>
                </div>
                {accountDraft && (
                  <form className="workspace-form" onSubmit={saveAccount}>
                    <div className="form-row">
                      <label>Họ và tên<input required maxLength={120} value={accountDraft.fullName}
                        onChange={(event) => setAccountDraft({ ...accountDraft, fullName: event.target.value })} /></label>
                      <label>Email<input required type="email" disabled={Boolean(accountDraft.id)} value={accountDraft.email}
                        onChange={(event) => setAccountDraft({ ...accountDraft, email: event.target.value })} /></label>
                    </div>
                    <div className="form-row">
                      <label>{accountDraft.id ? "Mật khẩu mới (để trống nếu không đổi)" : "Mật khẩu"}
                        <input required={!accountDraft.id} minLength={8} type="password" value={accountDraft.password}
                          onChange={(event) => setAccountDraft({ ...accountDraft, password: event.target.value })} />
                      </label>
                      <label>Số điện thoại<input maxLength={30} value={accountDraft.phone}
                        onChange={(event) => setAccountDraft({ ...accountDraft, phone: event.target.value })} /></label>
                    </div>
                    <div className="form-row">
                      <label>Giới tính<input maxLength={20} value={accountDraft.gender}
                        onChange={(event) => setAccountDraft({ ...accountDraft, gender: event.target.value })} /></label>
                      <label>Vai trò<select value={accountDraft.role}
                        onChange={(event) => setAccountDraft({ ...accountDraft, role: event.target.value as Role })}>
                        {roles.map((role) => <option key={role}>{role}</option>)}
                      </select></label>
                    </div>
                    {accountDraft.id && <label className="check-row"><input type="checkbox" checked={accountDraft.status === "ACTIVE"}
                      onChange={(event) => setAccountDraft({ ...accountDraft, status: event.target.checked ? "ACTIVE" : "LOCKED" })} />
                      Đang hoạt động</label>}
                    <div className="panel-subheading">
                      <button className="button button-dark button-small" disabled={busy}>Lưu tài khoản</button>
                      <button type="button" className="quiet-button" onClick={() => setAccountDraft(null)}>Hủy</button>
                    </div>
                  </form>
                )}
                <div className="panel-subheading">
                  <input aria-label="Tìm tài khoản" placeholder="Tìm tên, email hoặc số điện thoại"
                    value={search} onChange={(event) => setSearch(event.target.value)} />
                  <select aria-label="Lọc vai trò" value={roleFilter} onChange={(event) => setRoleFilter(event.target.value)}>
                    <option value="ALL">Tất cả vai trò</option>{roles.map((role) => <option key={role}>{role}</option>)}
                  </select>
                </div>
                <div className="table-scroll"><table className="workspace-table">
                  <thead><tr><th>Tên / Email</th><th>Liên hệ</th><th>Vai trò</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                  <tbody>{filteredAccounts.map((account) => <tr key={account.id}>
                    <td><strong>{account.fullName}</strong><small>{account.email}</small></td>
                    <td>{account.phone || "—"}<small>{account.gender || "—"}</small></td>
                    <td>{account.role}</td>
                    <td><span className={`status-label ${account.status === "LOCKED" ? "cancelled" : ""}`}>{account.status}</span></td>
                    <td><button className="quiet-button" onClick={() => editAccount(account)}>Sửa</button>{" "}
                      <button className="quiet-button" onClick={() => void toggleAccountStatus(account)}>
                        {account.status === "ACTIVE" ? "Khóa" : "Mở khóa"}
                      </button>
                    </td>
                  </tr>)}</tbody>
                </table></div>
              </section>
            )}

            {tab === "subjects" && (
              <div className="content-editor-grid">
                <section className="workspace-panel content-panel">
                  <div className="panel-heading"><h2>Môn học</h2>
                    <button className="button button-dark button-small" onClick={startNewSubject}>+ Tạo môn</button></div>
                  {subjectDraft && (
                    <form className="workspace-form" onSubmit={saveSubject}>
                      <label>Tên môn<input required maxLength={100} value={subjectDraft.name}
                        onChange={(event) => setSubjectDraft({ ...subjectDraft, name: event.target.value })} /></label>
                      <label>Mô tả<textarea required maxLength={1000} rows={4} value={subjectDraft.description}
                        onChange={(event) => setSubjectDraft({ ...subjectDraft, description: event.target.value })} /></label>
                      <label className="check-row"><input type="checkbox" checked={subjectDraft.active}
                        onChange={(event) => setSubjectDraft({ ...subjectDraft, active: event.target.checked })} />Đang hiển thị</label>
                      <div className="panel-subheading"><button className="button button-dark button-small" disabled={busy}>Lưu môn học</button>
                        <button type="button" className="quiet-button" onClick={() => setSubjectDraft(null)}>Hủy</button></div>
                    </form>
                  )}
                  <div className="panel-list">{subjects.map((subject) => (
                    <div key={subject.id} className="workspace-list-row">
                      <button className="list-primary" onClick={() => setSelectedSubjectId(subject.id)}>
                        <strong>{subject.name}</strong><small>{subject.active ? "Đang hiển thị" : "Đang ẩn"}</small>
                      </button>
                      <button className="quiet-button" onClick={() => setSubjectDraft({ ...subject })}>Sửa</button>
                      <button className="quiet-button" onClick={() => void toggleSubject(subject)}>
                        {subject.active ? "Ẩn" : "Hiện"}
                      </button>
                    </div>
                  ))}</div>
                </section>
                <div>
                  <section className="workspace-panel content-panel">
                    <div className="panel-heading"><h2>Chiều môn học</h2>
                      {selectedSubject && <button className="button button-dark button-small" onClick={startNewDimension}>+ Thêm chiều</button>}
                    </div>
                    {!selectedSubject && <p>Chọn hoặc tạo môn học trước.</p>}
                    {selectedSubject && <p className="workspace-muted">{selectedSubject.name}</p>}
                    {dimensionDraft && selectedSubject && (
                      <form className="workspace-form" onSubmit={saveDimension}>
                        <label>Tên chiều<input required maxLength={100} value={dimensionDraft.name}
                          onChange={(event) => setDimensionDraft({ ...dimensionDraft, name: event.target.value })} /></label>
                        <label>Mô tả<textarea maxLength={1000} value={dimensionDraft.description}
                          onChange={(event) => setDimensionDraft({ ...dimensionDraft, description: event.target.value })} /></label>
                        <div className="form-row">
                          <label>Thứ tự<input type="number" min={0} value={dimensionDraft.displayOrder}
                            onChange={(event) => setDimensionDraft({ ...dimensionDraft, displayOrder: Number(event.target.value) })} /></label>
                          <label className="check-row"><input type="checkbox" checked={dimensionDraft.active}
                            onChange={(event) => setDimensionDraft({ ...dimensionDraft, active: event.target.checked })} />Hoạt động</label>
                        </div>
                        <div className="panel-subheading"><button className="button button-dark button-small" disabled={busy}>Lưu chiều</button>
                          <button type="button" className="quiet-button" onClick={() => setDimensionDraft(null)}>Hủy</button></div>
                      </form>
                    )}
                    <div className="panel-list">{dimensions.map((dimension) => (
                      <div className="workspace-list-row" key={dimension.id}>
                        <span><strong>{dimension.name}</strong><small>{dimension.description} · {dimension.active ? "Hoạt động" : "Đã tắt"}</small></span>
                        <button className="quiet-button" onClick={() => setDimensionDraft({ ...dimension })}>Sửa</button>
                        <button className="remove-button" aria-label={`Xóa ${dimension.name}`} onClick={() => void deleteDimension(dimension)}>×</button>
                      </div>
                    ))}</div>
                  </section>
                  <section className="workspace-panel content-panel" style={{ marginTop: 20 }}>
                    <div className="panel-heading"><h2>Khóa học & gói giá</h2>
                      {selectedSubject && <button className="button button-dark button-small" onClick={startNewCourse}>+ Thêm khóa học</button>}
                    </div>
                    {courseDraft && (
                      <form className="workspace-form" onSubmit={saveCourse}>
                        <label>Tên khóa học<input required maxLength={180} value={courseDraft.title}
                          onChange={(event) => setCourseDraft({ ...courseDraft, title: event.target.value })} /></label>
                        <label>Mô tả<textarea required maxLength={3000} value={courseDraft.description}
                          onChange={(event) => setCourseDraft({ ...courseDraft, description: event.target.value })} /></label>
                        <div className="form-row">
                          <label>Danh mục<input required value={courseDraft.category}
                            onChange={(event) => setCourseDraft({ ...courseDraft, category: event.target.value })} /></label>
                          <label>Giảng viên<input required value={courseDraft.instructor}
                            onChange={(event) => setCourseDraft({ ...courseDraft, instructor: event.target.value })} /></label>
                        </div>
                        <div className="form-row">
                          <label>Cấp độ<input required value={courseDraft.level}
                            onChange={(event) => setCourseDraft({ ...courseDraft, level: event.target.value })} /></label>
                          <label>Thời lượng<input required value={courseDraft.duration}
                            onChange={(event) => setCourseDraft({ ...courseDraft, duration: event.target.value })} /></label>
                        </div>
                        <div className="form-row">
                          <label>Giá cơ sở<input required type="number" min={0} step="0.01" value={courseDraft.price}
                            onChange={(event) => setCourseDraft({ ...courseDraft, price: Number(event.target.value) })} /></label>
                          <label>Ảnh (URL)<input value={courseDraft.image}
                            onChange={(event) => setCourseDraft({ ...courseDraft, image: event.target.value })} /></label>
                        </div>
                        <label className="check-row"><input type="checkbox" checked={courseDraft.published}
                          onChange={(event) => setCourseDraft({ ...courseDraft, published: event.target.checked })} />Xuất bản khóa học</label>
                        <div className="panel-subheading"><h3>Gói giá</h3>
                          <button type="button" className="quiet-button" onClick={() => setCourseDraft({
                            ...courseDraft,
                            pricePackages: [...courseDraft.pricePackages, { name: "", price: 0, currency: "USD", accessDays: 30, published: true }],
                          })}>+ Thêm gói</button>
                        </div>
                        {courseDraft.pricePackages.map((pricePackage, index) => (
                          <div className="package-editor" key={pricePackage.id ?? `new-${index}`}>
                            <div className="form-row">
                              <label>Tên gói<input required value={pricePackage.name}
                                onChange={(event) => setCourseDraft({
                                  ...courseDraft,
                                  pricePackages: courseDraft.pricePackages.map((item, i) => i === index ? { ...item, name: event.target.value } : item),
                                })} /></label>
                              <label>Giá<input required type="number" min={0} step="0.01" value={pricePackage.price}
                                onChange={(event) => setCourseDraft({
                                  ...courseDraft,
                                  pricePackages: courseDraft.pricePackages.map((item, i) => i === index ? { ...item, price: Number(event.target.value) } : item),
                                })} /></label>
                            </div>
                            <div className="form-row">
                              <label>Tiền tệ<input required maxLength={3} value={pricePackage.currency}
                                onChange={(event) => setCourseDraft({
                                  ...courseDraft,
                                  pricePackages: courseDraft.pricePackages.map((item, i) => i === index ? { ...item, currency: event.target.value.toUpperCase() } : item),
                                })} /></label>
                              <label>Số ngày truy cập<input type="number" min={0} max={3650} value={pricePackage.accessDays}
                                onChange={(event) => setCourseDraft({
                                  ...courseDraft,
                                  pricePackages: courseDraft.pricePackages.map((item, i) => i === index ? { ...item, accessDays: Number(event.target.value) } : item),
                                })} /></label>
                            </div>
                            <div className="panel-subheading">
                              <label className="check-row"><input type="checkbox" checked={pricePackage.published}
                                onChange={(event) => setCourseDraft({
                                  ...courseDraft,
                                  pricePackages: courseDraft.pricePackages.map((item, i) => i === index ? { ...item, published: event.target.checked } : item),
                                })} />Đang bán</label>
                              <button type="button" className="remove-button" aria-label="Bỏ gói giá khỏi danh sách"
                                disabled={courseDraft.pricePackages.length === 1}
                                onClick={() => setCourseDraft({
                                  ...courseDraft,
                                  pricePackages: courseDraft.pricePackages.filter((_, i) => i !== index),
                                })}>×</button>
                            </div>
                          </div>
                        ))}
                        <div className="panel-subheading"><button className="button button-dark button-small" disabled={busy}>Lưu khóa học và gói</button>
                          <button type="button" className="quiet-button" onClick={() => setCourseDraft(null)}>Hủy</button></div>
                      </form>
                    )}
                    <div className="panel-list">{subjectCourses.map((course) => (
                      <div className="workspace-list-row" key={course.id}>
                        <span><strong>{course.title}</strong><small>{course.published ? "Đã xuất bản" : "Đang ẩn"} · {course.pricePackages.length} gói</small></span>
                        <button className="quiet-button" onClick={() => editCourse(course)}>Sửa / gói giá</button>
                      </div>
                    ))}</div>
                  </section>
                </div>
              </div>
            )}

            {tab === "lessons" && (
              <section className="workspace-panel content-panel">
                <div className="panel-heading"><h2>Bài học theo môn</h2>
                  <label className="workspace-label">Khóa học
                    <select value={selectedCourseId ?? ""} onChange={(event) => setSelectedCourseId(Number(event.target.value) || null)}>
                      <option value="">Chọn khóa học</option>
                      {courses.map((course) => <option key={course.id} value={course.id}>{course.subjectName} — {course.title}</option>)}
                    </select>
                  </label>
                  <button className="button button-dark button-small" disabled={selectedCourseId === null} onClick={startNewLesson}>+ Thêm bài học</button>
                </div>
                {lessonDraft && (
                  <form className="workspace-form" onSubmit={saveLesson}>
                    <div className="form-row">
                      <label>Tiêu đề<input required maxLength={160} value={lessonDraft.title}
                        onChange={(event) => setLessonDraft({ ...lessonDraft, title: event.target.value })} /></label>
                      <label>Thứ tự<input required type="number" min={1} max={10000} value={lessonDraft.displayOrder}
                        onChange={(event) => setLessonDraft({ ...lessonDraft, displayOrder: Number(event.target.value) })} /></label>
                    </div>
                    <label>Tóm tắt<input required maxLength={1000} value={lessonDraft.summary}
                      onChange={(event) => setLessonDraft({ ...lessonDraft, summary: event.target.value })} /></label>
                    <label>Nội dung<textarea required maxLength={12000} rows={6} value={lessonDraft.content}
                      onChange={(event) => setLessonDraft({ ...lessonDraft, content: event.target.value })} /></label>
                    <label>Video URL<input type="url" value={lessonDraft.videoUrl}
                      onChange={(event) => setLessonDraft({ ...lessonDraft, videoUrl: event.target.value })} /></label>
                    <label className="check-row"><input type="checkbox" checked={lessonDraft.published}
                      onChange={(event) => setLessonDraft({ ...lessonDraft, published: event.target.checked })} />Kích hoạt bài học</label>
                    <div className="panel-subheading"><button className="button button-dark button-small" disabled={busy}>Lưu bài học</button>
                      <button type="button" className="quiet-button" onClick={() => setLessonDraft(null)}>Hủy</button></div>
                  </form>
                )}
                <div className="table-scroll"><table className="workspace-table">
                  <thead><tr><th>Thứ tự</th><th>Bài học</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                  <tbody>{lessons.map((lesson) => <tr key={lesson.id}>
                    <td>{lesson.displayOrder}</td><td><strong>{lesson.title}</strong><small>{lesson.summary}</small></td>
                    <td><span className={`status-label ${lesson.published ? "" : "cancelled"}`}>{lesson.published ? "ACTIVE" : "INACTIVE"}</span></td>
                    <td><button className="quiet-button" onClick={() => setLessonDraft({
                      id: lesson.id,
                      title: lesson.title,
                      summary: lesson.summary,
                      content: lesson.content,
                      videoUrl: lesson.videoUrl || "",
                      displayOrder: lesson.displayOrder,
                      published: lesson.published,
                    })}>Sửa</button>{" "}
                      <button className="quiet-button" onClick={() => void toggleLesson(lesson)}>{lesson.published ? "Tắt" : "Bật"}</button>
                    </td>
                  </tr>)}</tbody>
                </table></div>
              </section>
            )}

            {tab === "questions" && (
              <section className="workspace-panel content-panel">
                <div className="panel-heading"><h2>Ngân hàng câu hỏi</h2>
                  <button className="button button-dark button-small" onClick={() => setQuestionDraft({
                    ...emptyQuestion,
                    subject: subjects[0]?.name ?? "",
                  })}>+ Thêm câu hỏi</button>
                </div>
                {questionDraft && (
                  <form className="workspace-form" onSubmit={saveQuestion}>
                    <div className="form-row">
                      <label>Môn học<select required value={questionDraft.subject}
                        onChange={(event) => setQuestionDraft({ ...questionDraft, subject: event.target.value })}>
                        <option value="">Chọn môn học</option>{subjects.map((subject) => <option key={subject.id}>{subject.name}</option>)}
                      </select></label>
                      <label>Bài học<input value={questionDraft.lessonTitle}
                        onChange={(event) => setQuestionDraft({ ...questionDraft, lessonTitle: event.target.value })} /></label>
                    </div>
                    <div className="form-row">
                      <label>Độ khó<select value={questionDraft.level}
                        onChange={(event) => setQuestionDraft({ ...questionDraft, level: event.target.value as Question["level"] })}>
                        <option value="EASY">EASY</option><option value="MEDIUM">MEDIUM</option><option value="HARD">HARD</option>
                      </select></label>
                      <label>Trạng thái<select value={questionDraft.status}
                        onChange={(event) => setQuestionDraft({ ...questionDraft, status: event.target.value as Question["status"] })}>
                        <option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option>
                      </select></label>
                    </div>
                    <label>Nội dung câu hỏi<textarea required rows={3} value={questionDraft.prompt}
                      onChange={(event) => setQuestionDraft({ ...questionDraft, prompt: event.target.value })} /></label>
                    <div className="form-row">
                      {questionDraft.options.map((option, index) => (
                        <label key={index}>Đáp án {index + 1}<input required value={option}
                          onChange={(event) => setQuestionDraft({
                            ...questionDraft,
                            options: questionDraft.options.map((value, optionIndex) => optionIndex === index ? event.target.value : value),
                          })} /></label>
                      ))}
                    </div>
                    <label>Đáp án đúng<select value={questionDraft.correctOptionIndex}
                      onChange={(event) => setQuestionDraft({ ...questionDraft, correctOptionIndex: Number(event.target.value) })}>
                      {questionDraft.options.map((option, index) => <option key={index} value={index}>{index + 1}. {option || "—"}</option>)}
                    </select></label>
                    <label>Giải thích<textarea rows={2} value={questionDraft.explanation}
                      onChange={(event) => setQuestionDraft({ ...questionDraft, explanation: event.target.value })} /></label>
                    <div className="panel-subheading"><button className="button button-dark button-small" disabled={busy}>Lưu câu hỏi</button>
                      <button type="button" className="quiet-button" onClick={() => setQuestionDraft(null)}>Hủy</button></div>
                  </form>
                )}
                <div className="panel-subheading">
                  <input placeholder="Tìm theo câu hỏi, môn hoặc bài học" value={questionSearch}
                    onChange={(event) => setQuestionSearch(event.target.value)} />
                  <select value={questionLevel} onChange={(event) => setQuestionLevel(event.target.value)}>
                    <option value="ALL">Mọi cấp độ</option><option value="EASY">EASY</option><option value="MEDIUM">MEDIUM</option><option value="HARD">HARD</option>
                  </select>
                  <select aria-label="Lọc trạng thái câu hỏi" value={questionStatus} onChange={(event) => setQuestionStatus(event.target.value)}>
                    <option value="ALL">Mọi trạng thái</option><option value="ACTIVE">ACTIVE</option><option value="INACTIVE">INACTIVE</option>
                  </select>
                </div>
                <div className="table-scroll"><table className="workspace-table">
                  <thead><tr><th>Câu hỏi</th><th>Môn / bài</th><th>Độ khó</th><th>Đáp án đúng</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                  <tbody>{filteredQuestions.map((question) => {
                    let correctAnswer = "—";
                    try {
                      correctAnswer = parseOptions(question.optionsJson)[question.correctOptionIndex] ?? "—";
                    } catch (reason) {
                      correctAnswer = reason instanceof Error ? "Dữ liệu đáp án lỗi" : "—";
                    }
                    return <tr key={question.id}>
                      <td><strong>{question.prompt}</strong><small>{question.explanation}</small></td>
                      <td>{question.subject}<small>{question.lessonTitle}</small></td><td>{question.level}</td>
                      <td>{correctAnswer}</td>
                      <td><span className={`status-label ${question.status === "INACTIVE" ? "cancelled" : ""}`}>{question.status}</span></td>
                      <td><button className="quiet-button" onClick={() => editQuestion(question)}>Sửa</button>{" "}
                        <button className="quiet-button" onClick={() => void toggleQuestion(question)}>{question.status === "ACTIVE" ? "Tắt" : "Bật"}</button>{" "}
                        <button className="quiet-button" onClick={() => void deleteQuestion(question)}>Xóa</button></td>
                    </tr>;
                  })}</tbody>
                </table></div>
              </section>
            )}

            {tab === "settings" && (
              <section className="workspace-panel content-panel">
                <div className="panel-heading"><h2>Cài đặt hệ thống</h2>
                  <button className="button button-dark button-small" onClick={startNewSetting}>+ Tạo cài đặt</button>
                </div>
                {settingDraft && (
                  <form className="workspace-form" onSubmit={saveSetting}>
                    <div className="form-row">
                      <label>Nhóm<input required maxLength={50} value={settingDraft.settingGroup}
                        onChange={(event) => setSettingDraft({ ...settingDraft, settingGroup: event.target.value })} /></label>
                      <label>Tên hiển thị<input required maxLength={100} value={settingDraft.name}
                        onChange={(event) => setSettingDraft({ ...settingDraft, name: event.target.value })} /></label>
                    </div>
                    <div className="form-row">
                      <label>Giá trị<input required maxLength={100} value={settingDraft.value}
                        onChange={(event) => setSettingDraft({ ...settingDraft, value: event.target.value })} /></label>
                      <label>Thứ tự<input required type="number" min={0} value={settingDraft.displayOrder}
                        onChange={(event) => setSettingDraft({ ...settingDraft, displayOrder: Number(event.target.value) })} /></label>
                    </div>
                    <label className="check-row"><input type="checkbox" checked={settingDraft.active}
                      onChange={(event) => setSettingDraft({ ...settingDraft, active: event.target.checked })} />Đang hoạt động</label>
                    <div className="panel-subheading"><button className="button button-dark button-small" disabled={busy}>Lưu cài đặt</button>
                      <button type="button" className="quiet-button" onClick={() => setSettingDraft(null)}>Hủy</button></div>
                  </form>
                )}
                <div className="table-scroll"><table className="workspace-table">
                  <thead><tr><th>Nhóm</th><th>Tên</th><th>Giá trị</th><th>Thứ tự</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                  <tbody>{settings.map((setting) => <tr key={setting.id}>
                    <td>{setting.settingGroup}</td><td>{setting.name}</td><td><code>{setting.value}</code></td>
                    <td>{setting.displayOrder}</td><td>{setting.active ? "Hoạt động" : "Vô hiệu"}</td>
                    <td><button className="quiet-button" onClick={() => setSettingDraft({ ...setting })}>Sửa</button>{" "}
                      <button className="quiet-button" onClick={() => void toggleSetting(setting)}>{setting.active ? "Tắt" : "Bật"}</button></td>
                  </tr>)}</tbody>
                </table></div>
              </section>
            )}
          </>
        )}
      </main>
    </div>
  );
}
