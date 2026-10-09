"use client";

import Link from "next/link";
import { useEffect, useMemo, useRef, useState } from "react";
import "../workspace.css";

type SessionUser = {
  id: number;
  fullName: string;
  email: string;
  role: string;
  gender?: string;
  mobile?: string;
  address?: string;
  avatar?: string;
};

type Enrollment = {
  id: number;
  courseId: number;
  courseTitle: string;
  category?: string;
  instructor?: string;
  progressPercent?: number;
  status: string;
  createdAt: string;
};

type CustomerRegistration = {
  id: number;
  courseId: number;
  courseTitle: string;
  pricePackageId: number;
  pricePackageName: string;
  fullName: string;
  email: string;
  phone: string;
  amount: number;
  currency: string;
  status: "SUBMITTED" | "PAID" | "CANCELLED";
  submittedAt: string;
  validFrom?: string;
  validTo?: string;
};

type Lesson = {
  id: number;
  title: string;
  type: "TOPIC" | "LESSON" | "QUIZ";
  summary: string;
  content: string;
  videoUrl: string | null;
  displayOrder: number;
  completed: boolean;
};

type QuizQuestion = {
  id: number;
  prompt: string;
  options: string[];
  correctOptionIndex: number;
  explanation?: string;
};

type QuizOverview = {
  id: number;
  title: string;
  description: string;
  durationMinutes: number;
  passingScore: number;
  questions: QuizQuestion[];
};

type QuizAttempt = {
  id: number;
  quizId: number;
  status: "IN_PROGRESS" | "SUBMITTED";
  score: number | null;
  passed: boolean;
  totalTimeSeconds: number;
  answers: Record<number, number>; // questionId -> selectedOptionIndex
  markedQuestions: number[]; // questionId[]
};

const initialSampleEnrollments: Enrollment[] = [
  {
    id: 1,
    courseId: 3,
    courseTitle: "Frontend, from first principles",
    category: "Development",
    instructor: "Alex Tran",
    progressPercent: 65,
    status: "ACTIVE",
    createdAt: "2026-09-15",
  },
  {
    id: 2,
    courseId: 1,
    courseTitle: "Design systems that scale",
    category: "Design",
    instructor: "Maya Nguyen",
    progressPercent: 30,
    status: "ACTIVE",
    createdAt: "2026-09-20",
  },
];

const initialSampleRegistrations: CustomerRegistration[] = [
  {
    id: 1001,
    courseId: 3,
    courseTitle: "Frontend, from first principles",
    pricePackageId: 301,
    pricePackageName: "Gói cơ bản (3 tháng)",
    fullName: "Nguyễn Học Viên",
    email: "customer@example.com",
    phone: "0901234567",
    amount: 59,
    currency: "USD",
    status: "PAID",
    submittedAt: "2026-09-15 10:30",
    validFrom: "2026-09-15",
    validTo: "2026-12-15",
  },
  {
    id: 1002,
    courseId: 2,
    courseTitle: "The curious data analyst",
    pricePackageId: 201,
    pricePackageName: "Gói tiêu chuẩn (6 tháng)",
    fullName: "Nguyễn Học Viên",
    email: "customer@example.com",
    phone: "0901234567",
    amount: 49,
    currency: "USD",
    status: "SUBMITTED",
    submittedAt: "2026-10-01 14:20",
    validFrom: "-",
    validTo: "-",
  },
];

const sampleQuiz: QuizOverview = {
  id: 301,
  title: "Đánh giá Năng lực: Kiến trúc Web & React Căn bản",
  description: "Bài thi trắc nghiệm kiểm tra kiến thức về DOM, Component Lifecycle, Hooks và tối ưu rendering.",
  durationMinutes: 15,
  passingScore: 70,
  questions: [
    {
      id: 1,
      prompt: "Trong React 19, Hook nào sau đây được khuyến nghị để tối ưu việc ghi nhớ một giá trị tính toán tốn kém?",
      options: ["useEffect", "useMemo", "useCallback", "useRef"],
      correctOptionIndex: 1,
      explanation: "useMemo lưu lại kết quả tính toán giữa các lần render và chỉ tính toán lại khi dependencies thay đổi.",
    },
    {
      id: 2,
      prompt: "Next.js App Router sử dụng mô hình Server Component theo mặc định nhằm mục đích chính nào?",
      options: [
        "Tăng kích thước bundle phía client",
        "Giảm JavaScript tải về trình duyệt và cải thiện thời gian tải trang đầu tiên (FCP)",
        "Loại bỏ hoàn toàn CSS",
        "Bắt buộc trang phải render lại liên tục",
      ],
      correctOptionIndex: 1,
      explanation: "Server Components được render hoàn toàn trên server, giảm đáng kể lượng JS gửi về client, giúp trang tải cực nhanh.",
    },
    {
      id: 3,
      prompt: "Phương thức HTTP nào an toàn (Safe) và Idempotent để truy vấn danh sách dữ liệu?",
      options: ["POST", "DELETE", "GET", "PATCH"],
      correctOptionIndex: 2,
      explanation: "Phương thức GET là safe (không làm thay đổi trạng thái tài nguyên) và idempotent (gọi nhiều lần cho kết quả như nhau).",
    },
    {
      id: 4,
      prompt: "Trong CSS Flexbox, thuộc tính nào căn chỉnh các item theo trục chính (Main Axis)?",
      options: ["align-items", "justify-content", "flex-direction", "align-content"],
      correctOptionIndex: 1,
      explanation: "justify-content dùng để căn chỉnh và phân bổ khoảng trống giữa các flex items dọc theo trục chính.",
    },
    {
      id: 5,
      prompt: "Mã trạng thái HTTP 401 Unauthorized thể hiện điều gì?",
      options: [
        "Máy chủ không tìm thấy trang yêu cầu",
        "Yêu cầu thành công nhưng không có nội dung",
        "Yêu cầu thiếu thông tin xác thực hoặc token không hợp lệ",
        "Máy chủ gặp lỗi nội bộ",
      ],
      correctOptionIndex: 2,
      explanation: "401 Unauthorized chỉ ra rằng request chưa được xác thực danh tính hợp lệ (thiếu Bearer token hoặc token hết hạn).",
    },
  ],
};

const sampleLessons: Lesson[] = [
  {
    id: 1,
    title: "1. Tổng quan Kiến trúc Web Hiện đại",
    type: "TOPIC",
    summary: "Hiểu rõ vòng đời của một HTTP Request và mô hình Client - Server.",
    content: "<h3>Giới thiệu mô hình Client - Server</h3><p>Mỗi khi người dùng gõ URL, trình duyệt gửi một HTTP Request tới máy chủ. Máy chủ xử lý và trả về phản hồi bao gồm HTML, CSS, JavaScript và dữ liệu JSON.</p>",
    videoUrl: null,
    displayOrder: 1,
    completed: true,
  },
  {
    id: 2,
    title: "2. Thực hành: Xây dựng Component Đầu tiên với React",
    type: "LESSON",
    summary: "Hướng dẫn chia nhỏ giao diện thành các thành phần độc lập, tái sử dụng.",
    content: "<p>Trong bài học này, chúng ta sẽ bắt đầu tạo các Functional Components, sử dụng Props để truyền dữ liệu và State để quản lý trạng thái tương tác.</p><p>Hãy chắc chắn bạn đã cài đặt Node.js và môi trường lập trình sẵn sàng.</p>",
    videoUrl: "https://www.youtube.com/embed/dQw4w9WgXcQ",
    displayOrder: 2,
    completed: true,
  },
  {
    id: 3,
    title: "3. Bài thi Trắc nghiệm: Đánh giá Năng lực Kiến trúc Web",
    type: "QUIZ",
    summary: "Bài kiểm tra 5 câu hỏi trắc nghiệm kiểm tra độ hiểu bài với thời gian 15 phút.",
    content: "<p>Bài kiểm tra đánh giá toàn diện nội dung học phần 1. Bạn cần đạt từ 70% điểm để vượt qua.</p>",
    videoUrl: null,
    displayOrder: 3,
    completed: false,
  },
];

export default function CustomerPortal() {
  const [token, setToken] = useState("");
  const [user, setUser] = useState<SessionUser>({
    id: 1,
    fullName: "Nguyễn Học Viên",
    email: "customer@example.com",
    role: "CUSTOMER",
    gender: "Nam",
    mobile: "0901234567",
    address: "Cầu Giấy, Hà Nội",
    avatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
  });

  // Main Tabs: "courses" | "registrations"
  const [mainTab, setMainTab] = useState<"courses" | "registrations">("courses");
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [profileTab, setProfileTab] = useState<"info" | "password">("info");

  // Enrollments & Registrations
  const [enrollments, setEnrollments] = useState<Enrollment[]>(initialSampleEnrollments);
  const [registrations, setRegistrations] = useState<CustomerRegistration[]>(initialSampleRegistrations);
  const [selectedCourseId, setSelectedCourseId] = useState<number>(3);

  // Lesson & Quiz State
  const [lessons, setLessons] = useState<Lesson[]>(sampleLessons);
  const [activeLesson, setActiveLesson] = useState<Lesson | null>(sampleLessons[1]);

  // Quiz Engine State
  const [quizState, setQuizState] = useState<"IDLE" | "TESTING" | "REVIEW">("IDLE");
  const [activeQuiz, setActiveQuiz] = useState<QuizOverview>(sampleQuiz);
  const [quizAttempt, setQuizAttempt] = useState<QuizAttempt>({
    id: 1,
    quizId: sampleQuiz.id,
    status: "IN_PROGRESS",
    score: null,
    passed: false,
    totalTimeSeconds: 0,
    answers: {},
    markedQuestions: [],
  });
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [timeLeft, setTimeLeft] = useState(sampleQuiz.durationMinutes * 60);
  const [paletteFilter, setPaletteFilter] = useState<"ALL" | "UNANSWERED" | "MARKED" | "ANSWERED">("ALL");

  // Notifications & Busy
  const [notice, setNotice] = useState("");

  const submitQuizRef = useRef<() => void>(() => {});

  // Sign out
  const signOut = () => {
    sessionStorage.removeItem("morrow.token");
    sessionStorage.removeItem("morrow.user");
    localStorage.removeItem("morrow.token");
    localStorage.removeItem("morrow.user");
    window.location.href = "/";
  };

  // Restore user session
  useEffect(() => {
    try {
      const storedToken = sessionStorage.getItem("morrow.token") || localStorage.getItem("morrow.token");
      const storedUser = sessionStorage.getItem("morrow.user") || localStorage.getItem("morrow.user");
      if (storedUser) {
        const parsed = JSON.parse(storedUser) as SessionUser;
        setTimeout(() => {
          setUser((prev) => ({ ...prev, ...parsed }));
          if (storedToken) setToken(storedToken);
        }, 0);
      }
    } catch {
      // Fallback to default customer
    }
  }, []);

  // Fetch real enrollments & registrations from API if available
  useEffect(() => {
    if (!token) return;
    fetch("/api/enrollments/me", { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then((data: Enrollment[]) => {
        if (Array.isArray(data) && data.length > 0) setEnrollments(data);
      })
      .catch(() => {});

    fetch("/api/registrations/me", { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then((data: CustomerRegistration[]) => {
        if (Array.isArray(data) && data.length > 0) setRegistrations(data);
      })
      .catch(() => {});
  }, [token]);

  // Timer countdown while in TESTING
  useEffect(() => {
    if (quizState !== "TESTING") return;
    const interval = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          submitQuizRef.current();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [quizState]);

  const formatTimer = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  // Select Option
  const handleSelectOption = (questionId: number, optionIdx: number) => {
    setQuizAttempt((prev) => ({
      ...prev,
      answers: { ...prev.answers, [questionId]: optionIdx },
    }));
  };

  // Toggle Mark for Review
  const toggleMarkForReview = (questionId: number) => {
    setQuizAttempt((prev) => {
      const isMarked = prev.markedQuestions.includes(questionId);
      return {
        ...prev,
        markedQuestions: isMarked
          ? prev.markedQuestions.filter((id) => id !== questionId)
          : [...prev.markedQuestions, questionId],
      };
    });
  };

  // Submit Quiz
  const submitQuiz = () => {
    let correctCount = 0;
    activeQuiz.questions.forEach((q) => {
      if (quizAttempt.answers[q.id] === q.correctOptionIndex) {
        correctCount++;
      }
    });

    const finalScore = Math.round((correctCount / activeQuiz.questions.length) * 100);
    const isPassed = finalScore >= activeQuiz.passingScore;

    setQuizAttempt((prev) => ({
      ...prev,
      status: "SUBMITTED",
      score: finalScore,
      passed: isPassed,
      totalTimeSeconds: activeQuiz.durationMinutes * 60 - timeLeft,
    }));

    setQuizState("REVIEW");
    setNotice(
      `Đã nộp bài thi thành công! Điểm số: ${finalScore}/100. ${isPassed ? "🎉 Bạn đã ĐẠT bài thi này!" : "⚠️ Chưa đạt điểm qua môn, bạn có thể xem lại lời giải bên dưới hoặc thi lại."}`
    );
  };
  useEffect(() => {
    submitQuizRef.current = submitQuiz;
  });

  // Retake Quiz
  const redoQuiz = () => {
    setQuizAttempt({
      id: Date.now(),
      quizId: activeQuiz.id,
      status: "IN_PROGRESS",
      score: null,
      passed: false,
      totalTimeSeconds: 0,
      answers: {},
      markedQuestions: [],
    });
    setTimeLeft(activeQuiz.durationMinutes * 60);
    setCurrentQuestionIndex(0);
    setQuizState("TESTING");
    setNotice("");
  };

  // Cancel Registration
  const cancelRegistration = (regId: number) => {
    setRegistrations((prev) =>
      prev.map((r) => (r.id === regId && r.status === "SUBMITTED" ? { ...r, status: "CANCELLED" } : r))
    );
    setNotice(`Đã hủy yêu cầu đăng ký #${regId}.`);
  };

  // Filtered Questions in Palette
  const filteredPaletteQuestions = useMemo(() => {
    return activeQuiz.questions.filter((q) => {
      const hasAnswered = quizAttempt.answers[q.id] !== undefined;
      const isMarked = quizAttempt.markedQuestions.includes(q.id);
      if (paletteFilter === "UNANSWERED") return !hasAnswered;
      if (paletteFilter === "ANSWERED") return hasAnswered;
      if (paletteFilter === "MARKED") return isMarked;
      return true;
    });
  }, [activeQuiz.questions, paletteFilter, quizAttempt]);

  const currentQuestion = activeQuiz.questions[currentQuestionIndex];

  return (
    <div className="workspace-page">
      {/* ================= WORKSPACE HEADER ================= */}
      <header className="workspace-header">
        <div style={{ display: "flex", alignItems: "center", gap: "28px" }}>
          <Link href="/" className="brand" style={{ fontSize: "22px" }}>
            OnlineLearn<span style={{ color: "var(--coral)" }}>.</span>
          </Link>

          {/* Navigation Tabs */}
          <div style={{ display: "flex", gap: "18px", fontSize: "13px" }}>
            <button
              onClick={() => { setMainTab("courses"); setQuizState("IDLE"); }}
              style={{
                border: 0,
                background: "none",
                cursor: "pointer",
                padding: "8px 0",
                fontWeight: mainTab === "courses" ? 700 : 400,
                color: mainTab === "courses" ? "var(--ink)" : "#6d7b71",
                borderBottom: mainTab === "courses" ? "2px solid var(--coral)" : "none",
              }}
            >
              📚 Khóa học của tôi ({enrollments.length})
            </button>
            <button
              onClick={() => { setMainTab("registrations"); setQuizState("IDLE"); }}
              style={{
                border: 0,
                background: "none",
                cursor: "pointer",
                padding: "8px 0",
                fontWeight: mainTab === "registrations" ? 700 : 400,
                color: mainTab === "registrations" ? "var(--ink)" : "#6d7b71",
                borderBottom: mainTab === "registrations" ? "2px solid var(--coral)" : "none",
              }}
            >
              📝 Đơn đăng ký của tôi ({registrations.length})
            </button>
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
          {/* User Identity Pill - Clickable to open Profile & Password Modal */}
          <div
            className="user-pill"
            onClick={() => { setShowProfileModal(true); setProfileTab("info"); }}
            title="Nhấp vào đây để xem Hồ sơ cá nhân & Đổi mật khẩu"
            style={{
              background: "rgba(255,255,255,0.95)",
              cursor: "pointer",
              transition: "all 0.2s cubic-bezier(0.4, 0, 0.2, 1)",
            }}
          >
            <div className="user-avatar-circle">
              {user.fullName ? user.fullName.charAt(0).toUpperCase() : "U"}
            </div>
            <div className="user-info-text">
              <span className="user-name-title" style={{ display: "flex", alignItems: "center", gap: "4px" }}>
                {user.fullName} <span style={{ fontSize: "10px", color: "var(--primary)" }}>⚙️</span>
              </span>
              <div>
                {user.role === "CUSTOMER" && <span className="role-badge role-customer" style={{ fontSize: "9.5px", padding: "2px 6px" }}>🎓 Học viên</span>}
                {user.role === "ADMIN" && <span className="role-badge role-admin" style={{ fontSize: "9.5px", padding: "2px 6px" }}>👑 Quản trị viên</span>}
                {user.role === "SALE" && <span className="role-badge role-sale" style={{ fontSize: "9.5px", padding: "2px 6px" }}>💼 Tư vấn Sale</span>}
                {user.role === "EXPERT" && <span className="role-badge role-expert" style={{ fontSize: "9.5px", padding: "2px 6px" }}>🔬 Chuyên gia</span>}
                {user.role === "MARKETING" && <span className="role-badge role-marketing" style={{ fontSize: "9.5px", padding: "2px 6px" }}>📢 Marketing</span>}
              </div>
            </div>
          </div>

          {["ADMIN", "SALE", "MARKETING", "EXPERT"].includes(user.role) && (
            <Link href="/manage" className="button button-small button-dark" style={{ display: "inline-flex", alignItems: "center", gap: "5px" }}>
              <span>⚙️</span> Cổng Quản trị
            </Link>
          )}

          <Link href="/" className="text-button" style={{ fontSize: "12.5px", color: "var(--ink)", fontWeight: 500 }}>
            🏠 Trang chủ
          </Link>

          <button onClick={signOut} className="text-button" style={{ fontSize: "12.5px", color: "var(--coral)", fontWeight: 600 }}>
            🚪 Đăng xuất
          </button>
        </div>
      </header>

      {/* Notification */}
      {notice && (
        <div style={{ background: "#eef6ea", borderBottom: "1px solid #cbe0c3", padding: "10px 5.5%", color: "#284a37", display: "flex", justifyContent: "space-between", alignItems: "center", fontSize: "13px" }}>
          <span>🔔 {notice}</span>
          <button onClick={() => setNotice("")} style={{ border: 0, background: "none", cursor: "pointer", fontWeight: 700 }}>✕</button>
        </div>
      )}

      {/* ================= TAB 1: MY COURSES & LESSON VIEW & QUIZ ================= */}
      {mainTab === "courses" && (
        <main className="workspace-main">
          {quizState === "IDLE" ? (
            <div className="workspace-grid" style={{ gridTemplateColumns: "300px minmax(0, 1fr)" }}>
              {/* Left Column: My Enrolled Courses & Lesson Sidebar */}
              <div style={{ display: "grid", gap: "20px" }}>
                {/* Enrolled Courses Selector */}
                <div className="workspace-panel">
                  <div className="panel-heading" style={{ marginBottom: "12px", paddingBottom: "8px" }}>
                    <h3 style={{ margin: 0, fontSize: "16px", fontWeight: 700 }}>Khóa học đã đăng ký</h3>
                  </div>
                  <div style={{ display: "grid", gap: "8px" }}>
                    {enrollments.map((enr) => (
                      <div
                        key={enr.id}
                        onClick={() => setSelectedCourseId(enr.courseId)}
                        style={{
                          padding: "12px",
                          borderRadius: "8px",
                          border: selectedCourseId === enr.courseId ? "2px solid var(--green)" : "1px solid #dce2d7",
                          background: selectedCourseId === enr.courseId ? "#eef4ec" : "#fff",
                          cursor: "pointer",
                          transition: "all 0.2s",
                        }}
                      >
                        <strong style={{ display: "block", fontSize: "13px", color: "var(--ink)", marginBottom: "4px" }}>
                          {enr.courseTitle}
                        </strong>
                        <div style={{ display: "flex", justifyContent: "space-between", fontSize: "11px", color: "var(--muted)" }}>
                          <span>{enr.instructor}</span>
                          <span style={{ fontWeight: 700, color: "var(--green)" }}>{enr.progressPercent || 0}% hoàn thành</span>
                        </div>
                        {/* Progress Bar */}
                        <div style={{ height: "4px", background: "#d9e2d5", borderRadius: "2px", marginTop: "8px", overflow: "hidden" }}>
                          <div style={{ width: `${enr.progressPercent || 0}%`, height: "100%", background: "var(--green)" }} />
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* Lesson List of Active Course */}
                <div className="workspace-panel">
                  <div className="panel-heading" style={{ marginBottom: "12px", paddingBottom: "8px" }}>
                    <h3 style={{ margin: 0, fontSize: "16px", fontWeight: 700 }}>Nội dung bài học</h3>
                  </div>
                  <div style={{ display: "grid", gap: "6px" }}>
                    {lessons.map((les) => (
                      <button
                        key={les.id}
                        onClick={() => setActiveLesson(les)}
                        style={{
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "space-between",
                          padding: "10px 12px",
                          border: 0,
                          borderRadius: "6px",
                          background: activeLesson?.id === les.id ? "#e2ebe0" : "transparent",
                          cursor: "pointer",
                          textAlign: "left",
                          color: "var(--ink)",
                          fontSize: "12px",
                        }}
                      >
                        <span style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                          <span>{les.type === "QUIZ" ? "📝" : les.type === "LESSON" ? "▶️" : "📖"}</span>
                          <span style={{ fontWeight: activeLesson?.id === les.id ? 700 : 400 }}>{les.title}</span>
                        </span>
                        {les.completed ? (
                          <span style={{ color: "#2e7d32", fontWeight: 700 }} title="Đã hoàn thành">✓</span>
                        ) : (
                          <span style={{ color: "#a5b3a8" }}>○</span>
                        )}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* Right Column: Lesson Viewer / Video / HTML */}
              <div className="workspace-panel" style={{ minHeight: "560px", padding: "32px" }}>
                {activeLesson ? (
                  <div>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", borderBottom: "1px solid #e2e7de", paddingBottom: "18px", marginBottom: "20px" }}>
                      <div>
                        <span className="slide-tag" style={{ background: activeLesson.type === "QUIZ" ? "var(--coral)" : "var(--green)" }}>
                          {activeLesson.type === "QUIZ" ? "BÀI THI TRẮC NGHIỆM" : activeLesson.type === "LESSON" ? "BÀI HỌC VIDEO" : "CHỦ ĐỀ LÝ THUYẾT"}
                        </span>
                        <h2 style={{ fontSize: "26px", margin: "8px 0 6px" }}>{activeLesson.title}</h2>
                        <p style={{ color: "var(--muted)", fontSize: "13px", margin: 0 }}>{activeLesson.summary}</p>
                      </div>

                      <button
                        onClick={() => {
                          setLessons((prev) =>
                            prev.map((l) => (l.id === activeLesson.id ? { ...l, completed: !l.completed } : l))
                          );
                          setActiveLesson((prev) => (prev ? { ...prev, completed: !prev.completed } : null));
                        }}
                        className={`button button-small ${activeLesson.completed ? "button-dark" : ""}`}
                        style={{ border: "1px solid #d4ded0" }}
                      >
                        {activeLesson.completed ? "✓ Đã hoàn thành" : "Đánh dấu hoàn thành"}
                      </button>
                    </div>

                    {/* If Lesson has Video */}
                    {activeLesson.videoUrl && (
                      <div style={{ marginBottom: "24px", borderRadius: "10px", overflow: "hidden", background: "#000", aspectRatio: "16/9" }}>
                        <iframe
                          src={activeLesson.videoUrl}
                          title={activeLesson.title}
                          style={{ width: "100%", height: "100%", border: 0 }}
                          allowFullScreen
                        />
                      </div>
                    )}

                    {/* Lesson HTML Content */}
                    <div
                      style={{ fontSize: "15px", lineHeight: "1.8", color: "var(--ink)" }}
                      dangerouslySetInnerHTML={{ __html: activeLesson.content }}
                    />

                    {/* If Lesson is a Quiz -> Display Quiz Starter */}
                    {activeLesson.type === "QUIZ" && (
                      <div style={{ marginTop: "32px", padding: "24px", background: "#f8faf6", border: "1px solid #dbe2d6", borderRadius: "10px" }}>
                        <h3 style={{ margin: "0 0 10px", fontSize: "18px", color: "var(--green)" }}>
                          📋 {activeQuiz.title}
                        </h3>
                        <p style={{ color: "var(--muted)", fontSize: "13px", marginBottom: "18px" }}>
                          {activeQuiz.description}
                        </p>
                        <div style={{ display: "flex", gap: "24px", fontSize: "13px", color: "var(--ink)", marginBottom: "24px" }}>
                          <span>⏱️ Thời gian: <strong>{activeQuiz.durationMinutes} phút</strong></span>
                          <span>❓ Số lượng: <strong>{activeQuiz.questions.length} câu</strong></span>
                          <span>🎯 Điểm đạt: <strong>{activeQuiz.passingScore}%</strong></span>
                        </div>
                        <button
                          onClick={() => {
                            setTimeLeft(activeQuiz.durationMinutes * 60);
                            setQuizState("TESTING");
                            setCurrentQuestionIndex(0);
                          }}
                          className="button button-accent"
                        >
                          Bắt đầu làm bài thi ngay →
                        </button>
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={{ textAlign: "center", padding: "60px 20px", color: "var(--muted)" }}>
                    Chọn một bài học từ danh sách bên trái để bắt đầu học.
                  </div>
                )}
              </div>
            </div>
          ) : quizState === "TESTING" ? (
            /* ================= QUIZ RUNNER (DOING TEST) ================= */
            <div>
              {/* Quiz Header with Live Countdown Timer */}
              <div className="quiz-runner-header">
                <div>
                  <span style={{ fontSize: "11px", fontWeight: 700, color: "var(--green)", textTransform: "uppercase" }}>
                    ĐANG LÀM BÀI KIỂM TRA
                  </span>
                  <h2 style={{ fontSize: "20px", margin: "4px 0 0" }}>{activeQuiz.title}</h2>
                </div>

                <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
                  <div className={`timer-badge ${timeLeft < 180 ? "warning" : ""}`}>
                    <span>⏱️</span>
                    <span>{formatTimer(timeLeft)}</span>
                  </div>
                  <button onClick={submitQuiz} className="button button-accent button-small">
                    Nộp bài thi (Score Exam)
                  </button>
                </div>
              </div>

              {/* Main Quiz Layout: Left Question, Right Palette */}
              <div className="quiz-runner-layout">
                {/* Question Area */}
                <div className="question-card">
                  <div className="question-header">
                    <span className="question-number">
                      CÂU HỎI {currentQuestionIndex + 1} / {activeQuiz.questions.length}
                    </span>
                    <button
                      type="button"
                      onClick={() => toggleMarkForReview(currentQuestion.id)}
                      className={`mark-review-btn ${quizAttempt.markedQuestions.includes(currentQuestion.id) ? "active" : ""}`}
                    >
                      🚩 {quizAttempt.markedQuestions.includes(currentQuestion.id) ? "Đã đánh dấu xem lại" : "Đánh dấu xem lại (Mark for Review)"}
                    </button>
                  </div>

                  <h3 className="question-prompt">{currentQuestion.prompt}</h3>

                  <div className="options-list">
                    {currentQuestion.options.map((opt, oIndex) => {
                      const isSelected = quizAttempt.answers[currentQuestion.id] === oIndex;
                      const letters = ["A", "B", "C", "D"];
                      return (
                        <div
                          key={oIndex}
                          onClick={() => handleSelectOption(currentQuestion.id, oIndex)}
                          className={`option-label ${isSelected ? "selected" : ""}`}
                        >
                          <span className="option-circle">{letters[oIndex]}</span>
                          <span style={{ fontSize: "14px", color: "var(--ink)" }}>{opt}</span>
                        </div>
                      );
                    })}
                  </div>

                  {/* Navigation Buttons */}
                  <div style={{ display: "flex", justifyContent: "space-between", marginTop: "32px", paddingTop: "20px", borderTop: "1px solid #edf0ea" }}>
                    <button
                      onClick={() => setCurrentQuestionIndex((prev) => Math.max(prev - 1, 0))}
                      disabled={currentQuestionIndex === 0}
                      className="button button-small"
                      style={{ border: "1px solid #d9e0d5" }}
                    >
                      ← Câu trước
                    </button>
                    <button
                      onClick={() => setCurrentQuestionIndex((prev) => Math.min(prev + 1, activeQuiz.questions.length - 1))}
                      disabled={currentQuestionIndex === activeQuiz.questions.length - 1}
                      className="button button-small button-dark"
                    >
                      Câu tiếp theo →
                    </button>
                  </div>
                </div>

                {/* Right Question Palette Jump */}
                <div className="palette-card">
                  <h4 style={{ margin: "0 0 12px", fontSize: "14px" }}>Bảng câu hỏi (Question Palette)</h4>

                  {/* Filter Pills */}
                  <div className="palette-filter">
                    <button
                      onClick={() => setPaletteFilter("ALL")}
                      className={`filter-pill ${paletteFilter === "ALL" ? "active" : ""}`}
                    >
                      Tất cả ({activeQuiz.questions.length})
                    </button>
                    <button
                      onClick={() => setPaletteFilter("UNANSWERED")}
                      className={`filter-pill ${paletteFilter === "UNANSWERED" ? "active" : ""}`}
                    >
                      Chưa làm ({activeQuiz.questions.length - Object.keys(quizAttempt.answers).length})
                    </button>
                    <button
                      onClick={() => setPaletteFilter("MARKED")}
                      className={`filter-pill ${paletteFilter === "MARKED" ? "active" : ""}`}
                    >
                      Đánh dấu ({quizAttempt.markedQuestions.length})
                    </button>
                    <button
                      onClick={() => setPaletteFilter("ANSWERED")}
                      className={`filter-pill ${paletteFilter === "ANSWERED" ? "active" : ""}`}
                    >
                      Đã làm ({Object.keys(quizAttempt.answers).length})
                    </button>
                  </div>

                  {/* Grid Numbers */}
                  <div className="palette-grid">
                    {filteredPaletteQuestions.map((q, idx) => {
                      const isCurrent = currentQuestionIndex === idx;
                      const isAnswered = quizAttempt.answers[q.id] !== undefined;
                      const isMarked = quizAttempt.markedQuestions.includes(q.id);

                      let cellClass = "palette-cell";
                      if (isMarked) cellClass += " marked";
                      else if (isAnswered) cellClass += " answered";
                      if (isCurrent) cellClass += " current";

                      return (
                        <button
                          key={q.id}
                          onClick={() => setCurrentQuestionIndex(idx)}
                          className={cellClass}
                          title={`Câu ${idx + 1}`}
                        >
                          {idx + 1}
                        </button>
                      );
                    })}
                  </div>

                  {/* Legend */}
                  <div style={{ fontSize: "11px", color: "var(--muted)", display: "grid", gap: "6px", borderTop: "1px solid #edf0ea", paddingTop: "12px" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <span style={{ width: "12px", height: "12px", background: "#2e7d32", borderRadius: "3px" }} />
                      <span>Đã chọn đáp án</span>
                    </div>
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <span style={{ width: "12px", height: "12px", background: "#f59e0b", borderRadius: "3px" }} />
                      <span>Đánh dấu cần xem lại</span>
                    </div>
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <span style={{ width: "12px", height: "12px", background: "#f7f9f5", border: "1px solid #d8dfd4", borderRadius: "3px" }} />
                      <span>Chưa trả lời</span>
                    </div>
                  </div>

                  <button
                    onClick={submitQuiz}
                    className="button button-accent"
                    style={{ width: "100%", marginTop: "20px" }}
                  >
                    Nộp bài thi ngay
                  </button>
                </div>
              </div>
            </div>
          ) : (
            /* ================= QUIZ REVIEW (XEM LẠI BÀI THI) ================= */
            <div className="workspace-panel" style={{ maxWidth: "900px", margin: "auto", padding: "36px" }}>
              <div style={{ textAlign: "center", paddingBottom: "24px", borderBottom: "1px solid #edf0ea", marginBottom: "30px" }}>
                <span style={{ fontSize: "48px" }}>{quizAttempt.passed ? "🏆" : "📝"}</span>
                <h2 style={{ fontSize: "28px", margin: "12px 0 6px" }}>
                  {quizAttempt.passed ? "Chúc mừng! Bạn đã ĐẠT bài thi" : "Kết quả: Chưa đạt điểm yêu cầu"}
                </h2>
                <div style={{ display: "inline-flex", gap: "20px", background: "#f0f4ed", padding: "12px 24px", borderRadius: "8px", marginTop: "12px" }}>
                  <div>
                    <span style={{ fontSize: "11px", color: "var(--muted)", display: "block" }}>ĐIỂM SỐ</span>
                    <strong style={{ fontSize: "22px", color: quizAttempt.passed ? "#2e7d32" : "#c2410c" }}>
                      {quizAttempt.score}%
                    </strong>
                  </div>
                  <div style={{ borderLeft: "1px solid #d4ded0", paddingLeft: "20px" }}>
                    <span style={{ fontSize: "11px", color: "var(--muted)", display: "block" }}>ĐIỂM ĐẠT</span>
                    <strong style={{ fontSize: "22px" }}>{activeQuiz.passingScore}%</strong>
                  </div>
                  <div style={{ borderLeft: "1px solid #d4ded0", paddingLeft: "20px" }}>
                    <span style={{ fontSize: "11px", color: "var(--muted)", display: "block" }}>THỜI GIAN</span>
                    <strong style={{ fontSize: "22px" }}>{formatTimer(quizAttempt.totalTimeSeconds)}</strong>
                  </div>
                </div>

                <div style={{ marginTop: "20px", display: "flex", justifyContent: "center", gap: "12px" }}>
                  <button onClick={redoQuiz} className="button button-accent button-small">
                    🔄 Làm lại bài thi (Redo Test)
                  </button>
                  <button onClick={() => setQuizState("IDLE")} className="button button-small" style={{ border: "1px solid #d9e0d5" }}>
                    Quay lại bài học
                  </button>
                </div>
              </div>

              {/* Review All Questions with Explanations */}
              <h3 style={{ fontSize: "20px", fontWeight: 700, marginBottom: "18px" }}>
                Chi tiết bài thi & Lời giải thích (Quiz Review):
              </h3>

              <div style={{ display: "grid", gap: "24px" }}>
                {activeQuiz.questions.map((q, qIndex) => {
                  const userAns = quizAttempt.answers[q.id];
                  const isCorrect = userAns === q.correctOptionIndex;
                  return (
                    <div
                      key={q.id}
                      style={{
                        padding: "20px",
                        border: "1px solid #dfe5dc",
                        borderRadius: "10px",
                        background: isCorrect ? "#fafffa" : "#fffbfa",
                      }}
                    >
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "12px" }}>
                        <strong style={{ color: "var(--green)" }}>Câu hỏi {qIndex + 1}:</strong>
                        <span
                          style={{
                            padding: "4px 10px",
                            borderRadius: "4px",
                            fontSize: "11px",
                            fontWeight: 700,
                            background: isCorrect ? "#e8f5e9" : "#ffebee",
                            color: isCorrect ? "#2e7d32" : "#c62828",
                          }}
                        >
                          {isCorrect ? "✓ Trả lời Đúng" : "✗ Trả lời Sai"}
                        </span>
                      </div>

                      <p style={{ fontSize: "15px", fontWeight: 700, color: "var(--ink)", marginBottom: "14px" }}>
                        {q.prompt}
                      </p>

                      <div style={{ display: "grid", gap: "8px" }}>
                        {q.options.map((opt, oIndex) => {
                          const isUserChoice = userAns === oIndex;
                          const isCorrectAns = q.correctOptionIndex === oIndex;

                          let bg = "#fff";
                          let border = "1px solid #e2e8df";
                          if (isCorrectAns) {
                            bg = "#e8f5e9";
                            border = "1px solid #81c784";
                          } else if (isUserChoice && !isCorrect) {
                            bg = "#ffebee";
                            border = "1px solid #e57373";
                          }

                          return (
                            <div
                              key={oIndex}
                              style={{
                                padding: "10px 14px",
                                borderRadius: "6px",
                                background: bg,
                                border: border,
                                fontSize: "13px",
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                              }}
                            >
                              <span>{opt}</span>
                              {isCorrectAns && <strong style={{ color: "#2e7d32", fontSize: "11px" }}>[Đáp án đúng]</strong>}
                              {isUserChoice && !isCorrectAns && <strong style={{ color: "#c62828", fontSize: "11px" }}>[Lựa chọn của bạn]</strong>}
                            </div>
                          );
                        })}
                      </div>

                      {/* Explanation Box */}
                      {q.explanation && (
                        <div className="explanation-box">
                          💡 <strong>Lời giải thích:</strong> {q.explanation}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </main>
      )}

      {/* ================= TAB 2: MY REGISTRATIONS ================= */}
      {mainTab === "registrations" && (
        <main className="workspace-main" style={{ maxWidth: "1100px" }}>
          <div className="workspace-heading">
            <div>
              <div className="eyebrow">
                <span className="eyebrow-line"></span>
                <span>Lịch sử giao dịch</span>
              </div>
              <h1>Đơn đăng ký của tôi (My Registrations)</h1>
            </div>
            <p className="workspace-muted">
              Theo dõi tình trạng xét duyệt, sửa hoặc hủy đơn khi còn ở trạng thái <code>Submitted</code>.
            </p>
          </div>

          <div className="workspace-panel sales-panel">
            <div className="table-scroll">
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Mã Đơn</th>
                    <th>Khóa học</th>
                    <th>Gói học</th>
                    <th>Thời gian nộp</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th>Hạn sử dụng</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {registrations.map((reg) => (
                    <tr key={reg.id}>
                      <td><strong>#{reg.id}</strong></td>
                      <td>
                        <strong>{reg.courseTitle}</strong>
                        <small>{reg.fullName} ({reg.phone})</small>
                      </td>
                      <td>{reg.pricePackageName}</td>
                      <td>{reg.submittedAt}</td>
                      <td><strong>${reg.amount} {reg.currency}</strong></td>
                      <td>
                        <span className={`status-label ${reg.status.toLowerCase()}`}>
                          {reg.status === "PAID" ? "ĐÃ THANH TOÁN" : reg.status === "SUBMITTED" ? "CHỜ DUYỆT" : "ĐÃ HỦY"}
                        </span>
                      </td>
                      <td>{reg.validFrom !== "-" ? `${reg.validFrom} → ${reg.validTo}` : "Chờ kích hoạt"}</td>
                      <td>
                        {reg.status === "SUBMITTED" ? (
                          <div style={{ display: "flex", gap: "6px" }}>
                            <button
                              onClick={() => cancelRegistration(reg.id)}
                              className="quiet-button"
                              style={{ color: "#c62828", borderColor: "#f2b8b8" }}
                              title="Hủy đơn đăng ký này"
                            >
                              Hủy đơn
                            </button>
                          </div>
                        ) : (
                          <span style={{ fontSize: "11px", color: "var(--muted)" }}>Không thể sửa</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </main>
      )}

      {/* ================= MODAL: USER PROFILE & CHANGE PASSWORD POP UP ================= */}
      {showProfileModal && (
        <div className="dialog-overlay" onClick={() => setShowProfileModal(false)}>
          <div className="dialog auth-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "560px", borderRadius: "20px" }}>
            <button className="dialog-close" onClick={() => setShowProfileModal(false)}>✕</button>

            {/* Profile User Header */}
            <div style={{ display: "flex", alignItems: "center", gap: "16px", marginBottom: "20px" }}>
              <div
                style={{
                  width: "56px",
                  height: "56px",
                  borderRadius: "50%",
                  background: "linear-gradient(135deg, var(--primary), #818cf8)",
                  color: "#ffffff",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "22px",
                  fontWeight: 800,
                  boxShadow: "0 4px 14px rgba(79, 70, 229, 0.3)",
                }}
              >
                {user.fullName ? user.fullName.charAt(0).toUpperCase() : "U"}
              </div>
              <div>
                <h3 style={{ margin: "0 0 4px", fontSize: "19px", fontWeight: 700, color: "var(--ink)" }}>
                  {user.fullName}
                </h3>
                <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                  {user.role === "CUSTOMER" && <span className="role-badge role-customer">🎓 Học viên</span>}
                  {user.role === "ADMIN" && <span className="role-badge role-admin">👑 Quản trị viên</span>}
                  {user.role === "SALE" && <span className="role-badge role-sale">💼 Tư vấn Sale</span>}
                  {user.role === "EXPERT" && <span className="role-badge role-expert">🔬 Chuyên gia</span>}
                  {user.role === "MARKETING" && <span className="role-badge role-marketing">📢 Marketing</span>}
                  <span style={{ fontSize: "11px", color: "var(--muted)" }}>{user.email}</span>
                </div>
              </div>
            </div>

            {/* Tab Segment: Profile vs Password */}
            <div className="auth-tab-segment">
              <button
                type="button"
                className={`auth-tab-btn ${profileTab === "info" ? "active" : ""}`}
                onClick={() => setProfileTab("info")}
              >
                👤 Thông tin cá nhân
              </button>
              <button
                type="button"
                className={`auth-tab-btn ${profileTab === "password" ? "active" : ""}`}
                onClick={() => setProfileTab("password")}
              >
                🔒 Đổi mật khẩu
              </button>
            </div>

            {/* Profile Info Tab Content */}
            {profileTab === "info" && (
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  const fd = new FormData(e.currentTarget);
                  const updated = {
                    ...user,
                    fullName: String(fd.get("fullName")),
                    gender: String(fd.get("gender")),
                    mobile: String(fd.get("mobile")),
                    address: String(fd.get("address")),
                  };
                  setUser(updated);
                  try {
                    sessionStorage.setItem("morrow.user", JSON.stringify(updated));
                    localStorage.setItem("morrow.user", JSON.stringify(updated));
                  } catch {}
                  setNotice("Đã cập nhật hồ sơ cá nhân thành công!");
                  setShowProfileModal(false);
                }}
                className="workspace-form"
                style={{ display: "grid", gap: "12px" }}
              >
                <div className="form-row">
                  <div>
                    <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>HỌ VÀ TÊN *</label>
                    <input type="text" name="fullName" defaultValue={user.fullName} required style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px" }} />
                  </div>
                  <div>
                    <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>GIỚI TÍNH</label>
                    <select name="gender" defaultValue={user.gender || "Nam"} style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px" }}>
                      <option value="Nam">Nam</option>
                      <option value="Nữ">Nữ</option>
                      <option value="Khác">Khác</option>
                    </select>
                  </div>
                </div>

                <div className="form-row">
                  <div>
                    <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>ĐỊA CHỈ EMAIL</label>
                    <input type="email" value={user.email} disabled style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px", background: "#f8fafc", color: "#64748b" }} />
                  </div>
                  <div>
                    <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>SỐ ĐIỆN THOẠI</label>
                    <input type="tel" name="mobile" defaultValue={user.mobile || ""} placeholder="0912 345 678" style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px" }} />
                  </div>
                </div>

                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>ĐỊA CHỈ LIÊN HỆ</label>
                  <input type="text" name="address" defaultValue={user.address || ""} placeholder="Số nhà, đường, quận, thành phố" style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px" }} />
                </div>

                <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px", marginTop: "10px" }}>
                  <button type="button" onClick={() => setShowProfileModal(false)} className="text-button">
                    Hủy bỏ
                  </button>
                  <button type="submit" className="button button-dark button-small" style={{ padding: "10px 18px" }}>
                    Lưu thay đổi hồ sơ
                  </button>
                </div>
              </form>
            )}

            {/* Change Password Tab Content */}
            {profileTab === "password" && (
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  const fd = new FormData(e.currentTarget);
                  const p1 = String(fd.get("newPass"));
                  const p2 = String(fd.get("confirmPass"));
                  if (p1 !== p2) {
                    setNotice("Mật khẩu mới và xác nhận mật khẩu không khớp!");
                    return;
                  }
                  setNotice("Đổi mật khẩu thành công!");
                  setShowProfileModal(false);
                }}
                className="workspace-form"
                style={{ display: "grid", gap: "12px" }}
              >
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>MẬT KHẨU HIỆN TẠI *</label>
                  <input type="password" name="oldPass" required placeholder="••••••••" style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px" }} />
                </div>
                <div className="form-row">
                  <div>
                    <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>MẬT KHẨU MỚI *</label>
                    <input type="password" name="newPass" required placeholder="Tối thiểu 6 ký tự" style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px" }} />
                  </div>
                  <div>
                    <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)" }}>XÁC NHẬN MẬT KHẨU MỚI *</label>
                    <input type="password" name="confirmPass" required placeholder="Nhập lại mật khẩu mới" style={{ width: "100%", padding: "9px 12px", border: "1px solid var(--line)", borderRadius: "8px" }} />
                  </div>
                </div>

                <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px", marginTop: "10px" }}>
                  <button type="button" onClick={() => setShowProfileModal(false)} className="text-button">
                    Hủy bỏ
                  </button>
                  <button type="submit" className="button button-accent button-small" style={{ padding: "10px 18px" }}>
                    Cập nhật mật khẩu mới
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}