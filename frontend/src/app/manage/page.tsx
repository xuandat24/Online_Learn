"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import "../workspace.css";

type SessionUser = {
  id: number;
  fullName: string;
  email: string;
  role: "ADMIN" | "SALE" | "MARKETING" | "EXPERT" | "CUSTOMER";
};

type AdminUser = {
  id: number;
  fullName: string;
  email: string;
  phone: string;
  gender: string;
  role: string;
  status: "ACTIVE" | "LOCKED";
  createdAt: string;
};

type Subject = {
  id: number;
  name: string;
  category: string;
  expertName?: string;
  expertId?: number;
  description: string;
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

type Registration = {
  id: number;
  courseTitle: string;
  pricePackageName: string;
  fullName: string;
  gender?: string;
  email: string;
  phone: string;
  amount: number;
  currency: string;
  status: "SUBMITTED" | "PAID" | "CANCELLED";
  submittedAt: string;
  validFrom?: string;
  validTo?: string;
  notes?: string;
};

type Post = {
  id: number;
  title: string;
  category: string;
  author: string;
  status: "PUBLISHED" | "DRAFT" | "HIDDEN";
  createdAt: string;
  summary: string;
  content: string;
};

type Slider = {
  id: number;
  title: string;
  tag: string;
  image: string;
  backlink: string;
  status: "ACTIVE" | "INACTIVE";
  displayOrder: number;
};

type QuestionBankItem = {
  id: number;
  subject: string;
  lessonTitle: string;
  level: "EASY" | "MEDIUM" | "HARD";
  prompt: string;
  options: string[];
  correctOptionIndex: number;
  explanation: string;
  status: "ACTIVE" | "INACTIVE";
};

type SystemSetting = {
  id: number;
  group: "USER_ROLES" | "POST_CATEGORIES" | "SUBJECT_CATEGORIES" | "QUESTION_LEVELS" | "LESSON_TYPES";
  name: string;
  value: string;
  displayOrder: number;
  active: boolean;
};

// Seed Mock Data
const initialUsers: AdminUser[] = [
  { id: 1, fullName: "Platform Administrator", email: "admin@example.com", phone: "0911000111", gender: "Nam", role: "ADMIN", status: "ACTIVE", createdAt: "2026-08-01" },
  { id: 2, fullName: "Nguyễn Văn Sale", email: "sale@example.com", phone: "0922000222", gender: "Nam", role: "SALE", status: "ACTIVE", createdAt: "2026-08-10" },
  { id: 3, fullName: "Lê Thị Marketing", email: "marketing@example.com", phone: "0933000333", gender: "Nữ", role: "MARKETING", status: "ACTIVE", createdAt: "2026-08-15" },
  { id: 4, fullName: "Alex Tran (Chuyên gia)", email: "expert@example.com", phone: "0944000444", gender: "Nam", role: "EXPERT", status: "ACTIVE", createdAt: "2026-08-20" },
  { id: 5, fullName: "Trần Học Viên", email: "customer@example.com", phone: "0955000555", gender: "Nữ", role: "CUSTOMER", status: "ACTIVE", createdAt: "2026-09-01" },
];

const initialSubjects: Subject[] = [
  { id: 1, name: "Phát triển Phần mềm", category: "Development", expertName: "Alex Tran", expertId: 4, description: "Các khóa học lập trình Web, Di động, Cloud và Database", active: true },
  { id: 2, name: "Thiết kế UI/UX & Đồ họa", category: "Design", expertName: "Maya Nguyen", expertId: 4, description: "Tư duy thiết kế sản phẩm số, Design System và Prototype", active: true },
  { id: 3, name: "Kinh doanh & Phân tích", category: "Business", expertName: "Jordan Lee", expertId: 4, description: "Phân tích dữ liệu doanh nghiệp và chiến lược thương hiệu", active: true },
  { id: 4, name: "Sáng tạo & Viết lách", category: "Creative", expertName: "Sam Rivera", expertId: 4, description: "Viết nội dung quảng cáo và nghệ thuật kể chuyện", active: true },
];

const initialRegistrations: Registration[] = [
  { id: 1001, courseTitle: "Frontend, from first principles", pricePackageName: "Gói cơ bản (3 tháng)", fullName: "Trần Học Viên", gender: "Nữ", email: "customer@example.com", phone: "0955000555", amount: 59, currency: "USD", status: "PAID", submittedAt: "2026-09-15 10:30", validFrom: "2026-09-15", validTo: "2026-12-15", notes: "Khách đã chuyển khoản qua Vietcombank" },
  { id: 1002, courseTitle: "The curious data analyst", pricePackageName: "Gói tiêu chuẩn (6 tháng)", fullName: "Lê Minh Tuấn", gender: "Nam", email: "tuan.le@gmail.com", phone: "0988776655", amount: 49, currency: "USD", status: "SUBMITTED", submittedAt: "2026-10-01 14:20", validFrom: "-", validTo: "-", notes: "Chờ gọi điện xác nhận thanh toán" },
  { id: 1003, courseTitle: "Design systems that scale", pricePackageName: "Gói trọn đời", fullName: "Phạm Hải Đăng", gender: "Nam", email: "dang.ph@yahoo.com", phone: "0912998877", amount: 69, currency: "USD", status: "SUBMITTED", submittedAt: "2026-10-02 08:15", validFrom: "-", validTo: "-", notes: "Cần tư vấn thêm phương thức thanh toán" },
];

const initialPosts: Post[] = [
  { id: 1, title: "Lộ trình tự học Frontend từ số 0 đến khi có việc làm", category: "Development", author: "Alex Tran", status: "PUBLISHED", createdAt: "2026-10-01", summary: "Những nguyên lý cốt lõi cần nắm vững về HTML/CSS/JS.", content: "Nội dung bài viết chi tiết..." },
  { id: 2, title: "5 nguyên tắc thiết kế Design System cho sản phẩm thực tế", category: "Design", author: "Maya Nguyen", status: "PUBLISHED", createdAt: "2026-09-28", summary: "Tạo ra ngôn ngữ thị giác nhất quán.", content: "Nội dung bài viết chi tiết..." },
  { id: 3, title: "Chiến lược Marketing Đa kênh cho doanh nghiệp vừa và nhỏ", category: "Business", author: "Lê Thị Marketing", status: "DRAFT", createdAt: "2026-10-02", summary: "Tối ưu ngân sách quảng cáo mạng xã hội.", content: "Nội dung bài viết chi tiết..." },
];

const initialSliders: Slider[] = [
  { id: 1, title: "Làm chủ Kỹ năng Số & Bứt phá Sự nghiệp 2026", tag: "Khóa học nổi bật", image: "https://images.unsplash.com/photo-1522202176988-66273c2fd55f", backlink: "/#courses", status: "ACTIVE", displayOrder: 1 },
  { id: 2, title: "Đăng ký Gói Trọn đời - Tiết kiệm đến 40%", tag: "Học phí ưu đãi", image: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3", backlink: "/#courses", status: "ACTIVE", displayOrder: 2 },
  { id: 3, title: "Hệ thống Thi Trắc nghiệm & Cấp Chứng chỉ", tag: "Đánh giá năng lực", image: "https://images.unsplash.com/photo-1434030216411-0b793f4b4173", backlink: "/learn", status: "ACTIVE", displayOrder: 3 },
];

const initialQuestionBank: QuestionBankItem[] = [
  { id: 1, subject: "Phát triển Phần mềm", lessonTitle: "React Fundamentals", level: "MEDIUM", prompt: "Hook nào dùng để ghi nhớ giá trị tính toán tốn kém?", options: ["useEffect", "useMemo", "useCallback", "useRef"], correctOptionIndex: 1, explanation: "useMemo ghi nhớ giá trị tính toán.", status: "ACTIVE" },
  { id: 2, subject: "Phát triển Phần mềm", lessonTitle: "HTTP & REST", level: "EASY", prompt: "Phương thức HTTP nào dùng để lấy dữ liệu mà không làm thay đổi trạng thái?", options: ["POST", "PUT", "GET", "DELETE"], correctOptionIndex: 2, explanation: "GET là phương thức safe và idempotent.", status: "ACTIVE" },
  { id: 3, subject: "Kinh doanh & Phân tích", lessonTitle: "SQL Queries", level: "HARD", prompt: "Mệnh đề nào dùng để lọc kết quả sau khi đã GROUP BY?", options: ["WHERE", "HAVING", "ORDER BY", "DISTINCT"], correctOptionIndex: 1, explanation: "HAVING được áp dụng sau khi dữ liệu đã được gom nhóm.", status: "ACTIVE" },
];

const initialSettings: SystemSetting[] = [
  { id: 1, group: "USER_ROLES", name: "Quản trị viên", value: "ADMIN", displayOrder: 1, active: true },
  { id: 2, group: "USER_ROLES", name: "Chuyên viên Bán hàng", value: "SALE", displayOrder: 2, active: true },
  { id: 3, group: "USER_ROLES", name: "Chuyên viên Tiếp thị", value: "MARKETING", displayOrder: 3, active: true },
  { id: 4, group: "USER_ROLES", name: "Giảng viên / Chuyên gia", value: "EXPERT", displayOrder: 4, active: true },
  { id: 5, group: "USER_ROLES", name: "Học viên", value: "CUSTOMER", displayOrder: 5, active: true },
  { id: 6, group: "POST_CATEGORIES", name: "Lập trình & Công nghệ", value: "TECH", displayOrder: 1, active: true },
  { id: 7, group: "POST_CATEGORIES", name: "Kinh nghiệm Thiết kế", value: "DESIGN", displayOrder: 2, active: true },
  { id: 8, group: "QUESTION_LEVELS", name: "Dễ (Easy)", value: "EASY", displayOrder: 1, active: true },
  { id: 9, group: "QUESTION_LEVELS", name: "Trung bình (Medium)", value: "MEDIUM", displayOrder: 2, active: true },
  { id: 10, group: "QUESTION_LEVELS", name: "Khó (Hard)", value: "HARD", displayOrder: 3, active: true },
];

export default function AdminPortal() {
  const [token, setToken] = useState("");
  const [user, setUser] = useState<SessionUser>({
    id: 1,
    fullName: "Platform Administrator",
    email: "admin@example.com",
    role: "ADMIN",
  });

  // Current active navigation item in Admin Sidebar:
  // "admin_dashboard" | "admin_users" | "admin_subjects" | "admin_settings"
  // "sale_dashboard" | "sale_registrations"
  // "mkt_dashboard" | "mkt_posts" | "mkt_sliders"
  // "expert_subjects" | "expert_questions"
  const [navKey, setNavKey] = useState<string>("admin_dashboard");

  // State Collections
  const [users, setUsers] = useState<AdminUser[]>(initialUsers);
  const [subjects, setSubjects] = useState<Subject[]>(initialSubjects);
  const [registrations, setRegistrations] = useState<Registration[]>(initialRegistrations);
  const [posts, setPosts] = useState<Post[]>(initialPosts);
  const [sliders, setSliders] = useState<Slider[]>(initialSliders);
  const [questions, setQuestions] = useState<QuestionBankItem[]>(initialQuestionBank);
  const [settings, setSettings] = useState<SystemSetting[]>(initialSettings);

  // Search & Filter States
  const [userSearch, setUserSearch] = useState("");
  const [userRoleFilter, setUserRoleFilter] = useState("ALL");
  const [regStatusFilter, setRegStatusFilter] = useState("ALL");
  const [regSearchEmail, setRegSearchEmail] = useState("");

  // Modals & Forms
  const [selectedReg, setSelectedReg] = useState<Registration | null>(null);
  const [selectedPost, setSelectedPost] = useState<Post | null>(null);
  const [showAddSubjectModal, setShowAddSubjectModal] = useState(false);
  const [showAddUserModal, setShowAddUserModal] = useState(false);
  const [showAddQuestionModal, setShowAddQuestionModal] = useState(false);
  const [showImportQuestionsModal, setShowImportQuestionsModal] = useState(false);
  const [showAddSliderModal, setShowAddSliderModal] = useState(false);

  // Notifications
  const [notice, setNotice] = useState("");

  // Restore user session
  useEffect(() => {
    const storedToken = sessionStorage.getItem("morrow.token");
    const storedUser = sessionStorage.getItem("morrow.user");
    if (storedUser) {
      try {
        const parsed = JSON.parse(storedUser) as SessionUser;
        setUser(parsed);
        if (storedToken) setToken(storedToken);
      } catch {}
    }
  }, []);

  // Fetch real users, subjects, registrations from backend
  useEffect(() => {
    if (!token) return;
    fetch("/api/admin/users", { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then((data: AdminUser[]) => {
        if (Array.isArray(data) && data.length > 0) setUsers(data);
      })
      .catch(() => {});

    fetch("/api/admin/subjects", { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then((data: Subject[]) => {
        if (Array.isArray(data) && data.length > 0) setSubjects(data);
      })
      .catch(() => {});

    fetch("/api/sales/registrations", { headers: { Authorization: `Bearer ${token}` } })
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then((data: Registration[]) => {
        if (Array.isArray(data) && data.length > 0) setRegistrations(data);
      })
      .catch(() => {});
  }, [token]);

  // Sale Confirm Payment & Provision Course
  const confirmPayment = async (regId: number) => {
    setRegistrations((prev) =>
      prev.map((r) =>
        r.id === regId
          ? {
              ...r,
              status: "PAID",
              validFrom: new Date().toISOString().split("T")[0],
              validTo: "2027-04-02",
              notes: "Đã duyệt thanh toán thành công và gửi email cấp quyền",
            }
          : r
      )
    );

    // Call real API if token exists
    if (token) {
      fetch(`/api/sales/registrations/${regId}/paid`, {
        method: "PATCH",
        headers: { Authorization: `Bearer ${token}` },
      }).catch(() => {});
    }

    setNotice(`Đã duyệt đơn #${regId} thành trạng thái PAID! Quyền học đã được cấp và gửi email thông tin đăng nhập cho học viên.`);
    setSelectedReg(null);
  };

  // Switch role test shortcut
  const switchRole = (newRole: "ADMIN" | "SALE" | "MARKETING" | "EXPERT") => {
    const updated = { ...user, role: newRole, fullName: `Demo ${newRole}` };
    setUser(updated);
    sessionStorage.setItem("morrow.user", JSON.stringify(updated));
    if (newRole === "SALE") setNavKey("sale_dashboard");
    else if (newRole === "MARKETING") setNavKey("mkt_dashboard");
    else if (newRole === "EXPERT") setNavKey("expert_subjects");
    else setNavKey("admin_dashboard");
    setNotice(`Đã chuyển vai trò quản lý sang: ${newRole}`);
  };

  // Filtered Users
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchRole = userRoleFilter === "ALL" || u.role === userRoleFilter;
      const q = userSearch.toLowerCase();
      const matchSearch =
        !q || `${u.fullName} ${u.email} ${u.phone} ${u.role}`.toLowerCase().includes(q);
      return matchRole && matchSearch;
    });
  }, [userRoleFilter, userSearch, users]);

  // Filtered Registrations
  const filteredRegistrations = useMemo(() => {
    return registrations.filter((r) => {
      const matchStatus = regStatusFilter === "ALL" || r.status === regStatusFilter;
      const q = regSearchEmail.toLowerCase();
      const matchSearch = !q || r.email.toLowerCase().includes(q) || r.fullName.toLowerCase().includes(q);
      return matchStatus && matchSearch;
    });
  }, [regSearchEmail, regStatusFilter, registrations]);

  return (
    <div className="admin-shell">
      {/* ================= ADMIN SIDEBAR ================= */}
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span className="brand-mark" style={{ width: "26px", height: "26px", fontSize: "18px" }}>O</span>
          <span>OnlineLearn</span>
        </div>

        {/* User Identity in Sidebar */}
        <div style={{ padding: "0 8px 18px", borderBottom: "1px solid rgba(255,255,255,0.08)", marginBottom: "18px" }}>
          <strong style={{ display: "block", color: "#fff", fontSize: "13px" }}>{user.fullName}</strong>
          <span className="actor-badge" style={{ marginTop: "4px", fontSize: "9px" }}>
            VAI TRÒ: {user.role}
          </span>
        </div>

        {/* Navigation Groups */}
        <div style={{ flex: 1, overflowY: "auto" }}>
          {/* GROUP 1: ADMINISTRATOR */}
          {(user.role === "ADMIN") && (
            <div className="admin-nav-group">
              <div className="admin-group-title">👑 QUẢN TRỊ VIÊN (ADMIN)</div>
              <button
                onClick={() => setNavKey("admin_dashboard")}
                className={`admin-nav-item ${navKey === "admin_dashboard" ? "active" : ""}`}
              >
                📊 Tổng quan Dashboard
              </button>
              <button
                onClick={() => setNavKey("admin_users")}
                className={`admin-nav-item ${navKey === "admin_users" ? "active" : ""}`}
              >
                👥 Quản lý Người dùng ({users.length})
              </button>
              <button
                onClick={() => setNavKey("admin_subjects")}
                className={`admin-nav-item ${navKey === "admin_subjects" ? "active" : ""}`}
              >
                📚 Quản lý Môn học ({subjects.length})
              </button>
              <button
                onClick={() => setNavKey("admin_settings")}
                className={`admin-nav-item ${navKey === "admin_settings" ? "active" : ""}`}
              >
                ⚙️ Cài đặt Hệ thống ({settings.length})
              </button>
            </div>
          )}

          {/* GROUP 2: SALE */}
          {(user.role === "ADMIN" || user.role === "SALE") && (
            <div className="admin-nav-group">
              <div className="admin-group-title">💼 PHÂN HỆ BÁN HÀNG (SALE)</div>
              <button
                onClick={() => setNavKey("sale_dashboard")}
                className={`admin-nav-item ${navKey === "sale_dashboard" ? "active" : ""}`}
              >
                📈 Thống kê Doanh số Sale
              </button>
              <button
                onClick={() => setNavKey("sale_registrations")}
                className={`admin-nav-item ${navKey === "sale_registrations" ? "active" : ""}`}
              >
                📑 Đơn đăng ký Khóa học ({registrations.length})
              </button>
            </div>
          )}

          {/* GROUP 3: MARKETING */}
          {(user.role === "ADMIN" || user.role === "MARKETING") && (
            <div className="admin-nav-group">
              <div className="admin-group-title">📢 PHÂN HỆ TIẾP THỊ (MARKETING)</div>
              <button
                onClick={() => setNavKey("mkt_dashboard")}
                className={`admin-nav-item ${navKey === "mkt_dashboard" ? "active" : ""}`}
              >
                📊 Dashboard Marketing
              </button>
              <button
                onClick={() => setNavKey("mkt_posts")}
                className={`admin-nav-item ${navKey === "mkt_posts" ? "active" : ""}`}
              >
                ✍️ Quản lý Bài viết & Tin ({posts.length})
              </button>
              <button
                onClick={() => setNavKey("mkt_sliders")}
                className={`admin-nav-item ${navKey === "mkt_sliders" ? "active" : ""}`}
              >
                🖼️ Quản lý Sliders/Banners ({sliders.length})
              </button>
            </div>
          )}

          {/* GROUP 4: EXPERT */}
          {(user.role === "ADMIN" || user.role === "EXPERT") && (
            <div className="admin-nav-group">
              <div className="admin-group-title">🎓 GIẢNG VIÊN / CHUYÊN GIA (EXPERT)</div>
              <button
                onClick={() => setNavKey("expert_subjects")}
                className={`admin-nav-item ${navKey === "expert_subjects" ? "active" : ""}`}
              >
                📖 Môn học được phân công
              </button>
              <button
                onClick={() => setNavKey("expert_questions")}
                className={`admin-nav-item ${navKey === "expert_questions" ? "active" : ""}`}
              >
                ❓ Ngân hàng Câu hỏi ({questions.length})
              </button>
            </div>
          )}
        </div>

        {/* Quick Role Switcher for Testing */}
        <div style={{ borderTop: "1px solid rgba(255,255,255,0.1)", paddingTop: "14px" }}>
          <span style={{ fontSize: "10px", color: "#8da394", display: "block", marginBottom: "8px" }}>
            CHUYỂN VAI TRÒ KIỂM THỬ:
          </span>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "6px" }}>
            <button onClick={() => switchRole("ADMIN")} className="actor-btn" style={{ fontSize: "10px", padding: "4px" }}>
              👑 Admin
            </button>
            <button onClick={() => switchRole("SALE")} className="actor-btn" style={{ fontSize: "10px", padding: "4px" }}>
              💼 Sale
            </button>
            <button onClick={() => switchRole("MARKETING")} className="actor-btn" style={{ fontSize: "10px", padding: "4px" }}>
              📢 Mkt
            </button>
            <button onClick={() => switchRole("EXPERT")} className="actor-btn" style={{ fontSize: "10px", padding: "4px" }}>
              🎓 Expert
            </button>
          </div>

          <div style={{ marginTop: "14px", display: "flex", justifyContent: "space-between" }}>
            <Link href="/" style={{ color: "#d8e6a2", fontSize: "11px" }}>
              ← Ra trang chủ
            </Link>
            <Link href="/learn" style={{ color: "#d8e6a2", fontSize: "11px" }}>
              Vào học viên →
            </Link>
          </div>
        </div>
      </aside>

      {/* ================= MAIN CONTENT WRAP ================= */}
      <main className="admin-content-wrap">
        {/* Top notification */}
        {notice && (
          <div className="workspace-message">
            <span>🔔 {notice}</span>
            <button onClick={() => setNotice("")}>✕</button>
          </div>
        )}

        {/* ================= 1. ADMIN DASHBOARD ================= */}
        {navKey === "admin_dashboard" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Tổng quan hệ thống</span>
                </div>
                <h1>Bảng Điều Khiển Quản Trị (Admin Dashboard)</h1>
              </div>
              <span className="workspace-user">Cập nhật: {new Date().toLocaleDateString("vi-VN")}</span>
            </div>

            {/* KPI Cards */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: "20px", marginBottom: "32px" }}>
              <div className="workspace-panel" style={{ padding: "20px" }}>
                <span style={{ fontSize: "11px", color: "var(--muted)", textTransform: "uppercase" }}>TỔNG DOANH THU</span>
                <strong style={{ display: "block", fontSize: "28px", color: "var(--green)", margin: "8px 0" }}>$14,850</strong>
                <span style={{ fontSize: "11px", color: "#2e7d32" }}>↑ 18.5% so với tháng trước</span>
              </div>
              <div className="workspace-panel" style={{ padding: "20px" }}>
                <span style={{ fontSize: "11px", color: "var(--muted)", textTransform: "uppercase" }}>TỔNG HỌC VIÊN</span>
                <strong style={{ display: "block", fontSize: "28px", color: "var(--ink)", margin: "8px 0" }}>{users.length * 120}</strong>
                <span style={{ fontSize: "11px", color: "#2e7d32" }}>↑ 24 học viên mới tuần này</span>
              </div>
              <div className="workspace-panel" style={{ padding: "20px" }}>
                <span style={{ fontSize: "11px", color: "var(--muted)", textTransform: "uppercase" }}>ĐƠN ĐĂNG KÝ MỚI</span>
                <strong style={{ display: "block", fontSize: "28px", color: "var(--coral)", margin: "8px 0" }}>{registrations.length}</strong>
                <span style={{ fontSize: "11px", color: "#d97706" }}>2 đơn chờ Sale xử lý</span>
              </div>
              <div className="workspace-panel" style={{ padding: "20px" }}>
                <span style={{ fontSize: "11px", color: "var(--muted)", textTransform: "uppercase" }}>SỐ MÔN HỌC HOẠT ĐỘNG</span>
                <strong style={{ display: "block", fontSize: "28px", color: "var(--ink)", margin: "8px 0" }}>{subjects.length}</strong>
                <span style={{ fontSize: "11px", color: "var(--muted)" }}>4 chuyên ngành chính</span>
              </div>
            </div>

            {/* Recent Orders Overview */}
            <div className="workspace-panel">
              <div className="panel-heading">
                <h2>Đơn đăng ký gần đây nhất</h2>
                <button onClick={() => setNavKey("sale_registrations")} className="button button-small button-dark">
                  Xem tất cả đơn
                </button>
              </div>
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Mã Đơn</th>
                    <th>Khách hàng</th>
                    <th>Khóa học</th>
                    <th>Học phí</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {registrations.slice(0, 3).map((r) => (
                    <tr key={r.id}>
                      <td><strong>#{r.id}</strong></td>
                      <td>{r.fullName} ({r.email})</td>
                      <td>{r.courseTitle}</td>
                      <td><strong>${r.amount}</strong></td>
                      <td>
                        <span className={`status-label ${r.status.toLowerCase()}`}>{r.status}</span>
                      </td>
                      <td>
                        {r.status === "SUBMITTED" && (
                          <button onClick={() => confirmPayment(r.id)} className="button button-small button-accent">
                            Duyệt thanh toán
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 2. ADMIN USERS MANAGEMENT ================= */}
        {navKey === "admin_users" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Quản lý thành viên</span>
                </div>
                <h1>Danh sách Tài khoản (Users Management)</h1>
              </div>
              <button onClick={() => setShowAddUserModal(true)} className="button button-dark">
                + Thêm Người dùng mới
              </button>
            </div>

            {/* Search & Role Filters */}
            <div className="workspace-panel" style={{ marginBottom: "20px", padding: "16px" }}>
              <div style={{ display: "grid", gridTemplateColumns: "1.5fr 1fr 1fr", gap: "16px", alignItems: "end" }}>
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, display: "block", marginBottom: "4px" }}>TÌM KIẾM</label>
                  <input
                    type="text"
                    placeholder="Tìm theo tên, email, số điện thoại..."
                    value={userSearch}
                    onChange={(e) => setUserSearch(e.target.value)}
                    style={{ width: "100%", padding: "8px 12px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, display: "block", marginBottom: "4px" }}>LỌC THEO VAI TRÒ</label>
                  <select
                    value={userRoleFilter}
                    onChange={(e) => setUserRoleFilter(e.target.value)}
                    style={{ width: "100%", padding: "8px 12px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                  >
                    <option value="ALL">Tất cả vai trò</option>
                    <option value="ADMIN">ADMIN (Quản trị)</option>
                    <option value="SALE">SALE (Bán hàng)</option>
                    <option value="MARKETING">MARKETING (Tiếp thị)</option>
                    <option value="EXPERT">EXPERT (Giảng viên)</option>
                    <option value="CUSTOMER">CUSTOMER (Học viên)</option>
                  </select>
                </div>
                <div style={{ textAlign: "right", fontSize: "12px", color: "var(--muted)" }}>
                  Tìm thấy <strong>{filteredUsers.length}</strong> tài khoản
                </div>
              </div>
            </div>

            {/* Users Table */}
            <div className="workspace-panel">
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Họ và Tên</th>
                    <th>Email</th>
                    <th>Số điện thoại</th>
                    <th>Giới tính</th>
                    <th>Vai trò (Role)</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredUsers.map((u) => (
                    <tr key={u.id}>
                      <td><strong>{u.fullName}</strong></td>
                      <td>{u.email}</td>
                      <td>{u.phone}</td>
                      <td>{u.gender}</td>
                      <td>
                        <select
                          value={u.role}
                          onChange={(e) => {
                            const newR = e.target.value;
                            setUsers((prev) => prev.map((item) => (item.id === u.id ? { ...item, role: newR } : item)));
                            setNotice(`Đã đổi vai trò của ${u.fullName} sang: ${newR}`);
                          }}
                          style={{ padding: "4px 8px", border: "1px solid #c8d4c4", borderRadius: "4px", fontSize: "11px", fontWeight: 700 }}
                        >
                          <option value="ADMIN">ADMIN</option>
                          <option value="SALE">SALE</option>
                          <option value="MARKETING">MARKETING</option>
                          <option value="EXPERT">EXPERT</option>
                          <option value="CUSTOMER">CUSTOMER</option>
                        </select>
                      </td>
                      <td>
                        <span className={`status-label ${u.status === "ACTIVE" ? "" : "cancelled"}`}>
                          {u.status === "ACTIVE" ? "HOẠT ĐỘNG" : "ĐÃ KHÓA"}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={() => {
                            setUsers((prev) =>
                              prev.map((item) =>
                                item.id === u.id ? { ...item, status: item.status === "ACTIVE" ? "LOCKED" : "ACTIVE" } : item
                              )
                            );
                            setNotice(`Đã đổi trạng thái tài khoản ${u.fullName}.`);
                          }}
                          className="quiet-button"
                        >
                          {u.status === "ACTIVE" ? "Khóa" : "Kích hoạt"}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 3. ADMIN SUBJECTS ================= */}
        {navKey === "admin_subjects" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Chương trình đào tạo</span>
                </div>
                <h1>Quản lý Môn học (Subjects & Experts)</h1>
              </div>
              <button onClick={() => setShowAddSubjectModal(true)} className="button button-dark">
                + Tạo Môn học Mới
              </button>
            </div>

            <div className="workspace-panel">
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Tên Môn học</th>
                    <th>Chuyên ngành</th>
                    <th>Giảng viên Phụ trách</th>
                    <th>Mô tả</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {subjects.map((sub) => (
                    <tr key={sub.id}>
                      <td><strong>{sub.name}</strong></td>
                      <td>{sub.category}</td>
                      <td>
                        <span style={{ fontWeight: 700, color: "var(--green)" }}>{sub.expertName || "Chưa phân công"}</span>
                      </td>
                      <td>{sub.description}</td>
                      <td>
                        <span className={`status-label ${sub.active ? "" : "cancelled"}`}>
                          {sub.active ? "XUẤT BẢN" : "ẨN"}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={() => {
                            setSubjects((prev) =>
                              prev.map((s) => (s.id === sub.id ? { ...s, active: !s.active } : s))
                            );
                            setNotice(`Đã cập nhật trạng thái môn ${sub.name}.`);
                          }}
                          className="quiet-button"
                        >
                          {sub.active ? "Ẩn môn" : "Xuất bản"}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 4. ADMIN SYSTEM SETTINGS ================= */}
        {navKey === "admin_settings" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Cấu hình từ điển</span>
                </div>
                <h1>Cài đặt Hệ thống (System Settings)</h1>
              </div>
            </div>

            <div className="workspace-panel">
              <div className="panel-heading">
                <h2>Danh mục từ điển hệ thống</h2>
                <span className="workspace-muted">Bao gồm Vai trò, Phân loại bài viết, Mức độ câu hỏi, v.v.</span>
              </div>
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Nhóm Cài đặt</th>
                    <th>Tên hiển thị</th>
                    <th>Mã giá trị</th>
                    <th>Thứ tự</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {settings.map((s) => (
                    <tr key={s.id}>
                      <td><strong>{s.group}</strong></td>
                      <td>{s.name}</td>
                      <td><code>{s.value}</code></td>
                      <td>{s.displayOrder}</td>
                      <td>
                        <span className={`status-label ${s.active ? "" : "cancelled"}`}>
                          {s.active ? "HOẠT ĐỘNG" : "VÔ HIỆU"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 5. SALE REGISTRATIONS ================= */}
        {navKey === "sale_registrations" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Bộ phận Bán hàng</span>
                </div>
                <h1>Quản lý Đơn đăng ký (Registrations List)</h1>
              </div>
              <span className="workspace-user">Tổng: {registrations.length} đơn</span>
            </div>

            {/* Filter Bar */}
            <div className="workspace-panel" style={{ marginBottom: "20px", padding: "16px" }}>
              <div style={{ display: "grid", gridTemplateColumns: "1.5fr 1fr 1fr", gap: "16px", alignItems: "end" }}>
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, display: "block", marginBottom: "4px" }}>TÌM EMAIL / TÊN KHÁCH</label>
                  <input
                    type="text"
                    placeholder="Nhập email hoặc tên..."
                    value={regSearchEmail}
                    onChange={(e) => setRegSearchEmail(e.target.value)}
                    style={{ width: "100%", padding: "8px 12px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                  />
                </div>
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, display: "block", marginBottom: "4px" }}>LỌC THEO TRẠNG THÁI</label>
                  <select
                    value={regStatusFilter}
                    onChange={(e) => setRegStatusFilter(e.target.value)}
                    style={{ width: "100%", padding: "8px 12px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                  >
                    <option value="ALL">Tất cả trạng thái</option>
                    <option value="SUBMITTED">SUBMITTED (Chờ duyệt)</option>
                    <option value="PAID">PAID (Đã thanh toán)</option>
                    <option value="CANCELLED">CANCELLED (Đã hủy)</option>
                  </select>
                </div>
                <div style={{ textAlign: "right" }}>
                  <button
                    onClick={() => {
                      const newReg: Registration = {
                        id: Date.now(),
                        courseTitle: "Frontend, from first principles",
                        pricePackageName: "Gói cơ bản",
                        fullName: "Khách gọi hotline",
                        email: `hotline_${Date.now()}@example.com`,
                        phone: "0900111222",
                        amount: 59,
                        currency: "USD",
                        status: "SUBMITTED",
                        submittedAt: new Date().toISOString().replace("T", " ").substring(0, 16),
                        notes: "Tạo thủ công bởi Sale",
                      };
                      setRegistrations([newReg, ...registrations]);
                      setNotice("Đã tạo thêm đơn đăng ký mới thành công!");
                    }}
                    className="button button-small button-dark"
                  >
                    + Tạo đơn thủ công (Sale)
                  </button>
                </div>
              </div>
            </div>

            {/* Registrations Table */}
            <div className="workspace-panel">
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Mã Đơn</th>
                    <th>Khách hàng</th>
                    <th>Khóa học & Gói</th>
                    <th>Thời gian nộp</th>
                    <th>Tổng tiền</th>
                    <th>Trạng thái</th>
                    <th>Ghi chú</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredRegistrations.map((r) => (
                    <tr key={r.id}>
                      <td><strong>#{r.id}</strong></td>
                      <td>
                        <strong>{r.fullName}</strong>
                        <small>{r.email} • {r.phone}</small>
                      </td>
                      <td>
                        <strong>{r.courseTitle}</strong>
                        <small>{r.pricePackageName}</small>
                      </td>
                      <td>{r.submittedAt}</td>
                      <td><strong>${r.amount}</strong></td>
                      <td>
                        <span className={`status-label ${r.status.toLowerCase()}`}>
                          {r.status === "PAID" ? "ĐÃ THANH TOÁN" : r.status === "SUBMITTED" ? "CHỜ DUYỆT" : "ĐÃ HỦY"}
                        </span>
                      </td>
                      <td>
                        <span style={{ fontSize: "11px", color: "var(--muted)" }}>{r.notes || "-"}</span>
                      </td>
                      <td>
                        {r.status === "SUBMITTED" ? (
                          <div style={{ display: "flex", gap: "6px" }}>
                            <button
                              onClick={() => confirmPayment(r.id)}
                              className="button button-small button-accent"
                              title="Xác nhận khách đã nộp tiền, tạo tài khoản và kích hoạt khóa học"
                            >
                              Duyệt Paid
                            </button>
                            <button
                              onClick={() => {
                                setRegistrations((prev) =>
                                  prev.map((item) => (item.id === r.id ? { ...item, status: "CANCELLED" } : item))
                                );
                                setNotice(`Đã hủy đơn #${r.id}.`);
                              }}
                              className="quiet-button"
                            >
                              Hủy
                            </button>
                          </div>
                        ) : (
                          <span style={{ fontSize: "11px", color: "var(--green)", fontWeight: 700 }}>✓ Đã hoàn tất</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 6. SALE DASHBOARD ================= */}
        {navKey === "sale_dashboard" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Hiệu suất kinh doanh</span>
                </div>
                <h1>Thống Kê Doanh Số Sale (Sale Dashboard)</h1>
              </div>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "20px", marginBottom: "30px" }}>
              <div className="workspace-panel" style={{ padding: "24px" }}>
                <span style={{ fontSize: "11px", color: "var(--muted)" }}>DOANH SỐ ĐÃ THU</span>
                <strong style={{ display: "block", fontSize: "32px", color: "#2e7d32", margin: "10px 0" }}>
                  ${registrations.filter((r) => r.status === "PAID").reduce((sum, r) => sum + r.amount, 0)}
                </strong>
                <span style={{ fontSize: "12px", color: "var(--muted)" }}>Từ các đơn đã xác nhận</span>
              </div>
              <div className="workspace-panel" style={{ padding: "24px" }}>
                <span style={{ fontSize: "11px", color: "var(--muted)" }}>DOANH SỐ TIỀM NĂNG CHỜ THU</span>
                <strong style={{ display: "block", fontSize: "32px", color: "var(--coral)", margin: "10px 0" }}>
                  ${registrations.filter((r) => r.status === "SUBMITTED").reduce((sum, r) => sum + r.amount, 0)}
                </strong>
                <span style={{ fontSize: "12px", color: "var(--muted)" }}>Đơn SUBMITTED đang chờ liên hệ</span>
              </div>
              <div className="workspace-panel" style={{ padding: "24px" }}>
                <span style={{ fontSize: "11px", color: "var(--muted)" }}>TỶ LỆ CHỐT THÀNH CÔNG</span>
                <strong style={{ display: "block", fontSize: "32px", color: "var(--green)", margin: "10px 0" }}>
                  78.5%
                </strong>
                <span style={{ fontSize: "12px", color: "var(--muted)" }}>Vượt chỉ tiêu tháng này</span>
              </div>
            </div>
          </div>
        )}

        {/* ================= 7. MARKETING POSTS ================= */}
        {navKey === "mkt_posts" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Truyền thông & Blog</span>
                </div>
                <h1>Quản lý Bài viết & Tin tức (Posts Management)</h1>
              </div>
              <button
                onClick={() => {
                  const newPost: Post = {
                    id: Date.now(),
                    title: "Bài viết mới đăng bởi Marketing",
                    category: "Development",
                    author: user.fullName,
                    status: "PUBLISHED",
                    createdAt: new Date().toISOString().split("T")[0],
                    summary: "Tóm tắt ngắn gọn nội dung bài viết mới...",
                    content: "Nội dung bài viết mới đầy đủ...",
                  };
                  setPosts([newPost, ...posts]);
                  setNotice("Đã thêm bài viết mới thành công!");
                }}
                className="button button-dark"
              >
                + Thêm Bài viết Mới
              </button>
            </div>

            <div className="workspace-panel">
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Tiêu đề Bài viết</th>
                    <th>Chuyên mục</th>
                    <th>Tác giả</th>
                    <th>Ngày đăng</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {posts.map((p) => (
                    <tr key={p.id}>
                      <td><strong>{p.title}</strong></td>
                      <td>{p.category}</td>
                      <td>{p.author}</td>
                      <td>{p.createdAt}</td>
                      <td>
                        <span className={`status-label ${p.status === "PUBLISHED" ? "" : "submitted"}`}>
                          {p.status}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={() => {
                            setPosts((prev) =>
                              prev.map((item) =>
                                item.id === p.id ? { ...item, status: item.status === "PUBLISHED" ? "DRAFT" : "PUBLISHED" } : item
                              )
                            );
                            setNotice(`Đã đổi trạng thái bài viết "${p.title}".`);
                          }}
                          className="quiet-button"
                        >
                          {p.status === "PUBLISHED" ? "Chuyển Nháp" : "Xuất bản"}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 8. MARKETING SLIDERS ================= */}
        {navKey === "mkt_sliders" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Banner trang chủ</span>
                </div>
                <h1>Quản lý Sliders & Banners</h1>
              </div>
              <button
                onClick={() => {
                  const newSlider: Slider = {
                    id: Date.now(),
                    title: "Ưu đãi Khóa học Mùa thu 2026",
                    tag: "Khuyến mãi",
                    image: "https://images.unsplash.com/photo-1522202176988-66273c2fd55f",
                    backlink: "/#courses",
                    status: "ACTIVE",
                    displayOrder: sliders.length + 1,
                  };
                  setSliders([...sliders, newSlider]);
                  setNotice("Đã thêm banner slider mới!");
                }}
                className="button button-dark"
              >
                + Thêm Slider Mới
              </button>
            </div>

            <div className="workspace-panel">
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Tiêu đề Slider</th>
                    <th>Nhãn (Tag)</th>
                    <th>Liên kết (Backlink)</th>
                    <th>Thứ tự</th>
                    <th>Trạng thái</th>
                    <th>Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {sliders.map((s) => (
                    <tr key={s.id}>
                      <td><strong>{s.title}</strong></td>
                      <td>{s.tag}</td>
                      <td><code>{s.backlink}</code></td>
                      <td>{s.displayOrder}</td>
                      <td>
                        <span className={`status-label ${s.status === "ACTIVE" ? "" : "cancelled"}`}>
                          {s.status}
                        </span>
                      </td>
                      <td>
                        <button
                          onClick={() => {
                            setSliders((prev) =>
                              prev.map((item) =>
                                item.id === s.id ? { ...item, status: item.status === "ACTIVE" ? "INACTIVE" : "ACTIVE" } : item
                              )
                            );
                            setNotice(`Đã đổi trạng thái slider "${s.title}".`);
                          }}
                          className="quiet-button"
                        >
                          {s.status === "ACTIVE" ? "Tắt" : "Bật"}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 9. EXPERT QUESTION BANK & IMPORT ================= */}
        {navKey === "expert_questions" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Khảo thí & Đề thi</span>
                </div>
                <h1>Ngân Hàng Câu Hỏi (Question Bank)</h1>
              </div>
              <div style={{ display: "flex", gap: "10px" }}>
                <button
                  onClick={() => {
                    // Simulate CSV import
                    const imported: QuestionBankItem = {
                      id: Date.now(),
                      subject: "Phát triển Phần mềm",
                      lessonTitle: "CSS Layouts",
                      level: "EASY",
                      prompt: "Thuộc tính nào trong CSS Flexbox dùng để căn chỉnh trên Cross Axis?",
                      options: ["justify-content", "align-items", "flex-wrap", "order"],
                      correctOptionIndex: 1,
                      explanation: "align-items căn chỉnh items theo Cross Axis.",
                      status: "ACTIVE",
                    };
                    setQuestions([imported, ...questions]);
                    setNotice("Import thành công 1 câu hỏi từ file mẫu CSV!");
                  }}
                  className="button button-small"
                  style={{ background: "#eef3ea", border: "1px solid #cfe0cb" }}
                >
                  📥 Import câu hỏi (CSV / File)
                </button>
                <button
                  onClick={() => {
                    const newQ: QuestionBankItem = {
                      id: Date.now(),
                      subject: "Phát triển Phần mềm",
                      lessonTitle: "JavaScript Async",
                      level: "MEDIUM",
                      prompt: "Phương thức Promise.all() sẽ reject khi nào?",
                      options: [
                        "Khi tất cả promise đều hoàn thành",
                        "Khi có ít nhất 1 promise trong danh sách bị reject",
                        "Sau 5 giây",
                        "Không bao giờ reject",
                      ],
                      correctOptionIndex: 1,
                      explanation: "Promise.all fail-fast ngay khi có 1 promise bị reject.",
                      status: "ACTIVE",
                    };
                    setQuestions([newQ, ...questions]);
                    setNotice("Đã thêm câu hỏi mới vào ngân hàng đề thi!");
                  }}
                  className="button button-small button-dark"
                >
                  + Thêm Câu hỏi Mới
                </button>
              </div>
            </div>

            <div className="workspace-panel">
              <table className="workspace-table">
                <thead>
                  <tr>
                    <th>Câu hỏi (Prompt)</th>
                    <th>Môn học</th>
                    <th>Cấp độ</th>
                    <th>Đáp án đúng</th>
                    <th>Lời giải thích</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {questions.map((q) => (
                    <tr key={q.id}>
                      <td><strong>{q.prompt}</strong></td>
                      <td>{q.subject}</td>
                      <td>
                        <span
                          style={{
                            padding: "3px 8px",
                            borderRadius: "4px",
                            fontSize: "10px",
                            fontWeight: 700,
                            background: q.level === "EASY" ? "#e8f5e9" : q.level === "MEDIUM" ? "#fff3e0" : "#ffebee",
                            color: q.level === "EASY" ? "#2e7d32" : q.level === "MEDIUM" ? "#e65100" : "#c62828",
                          }}
                        >
                          {q.level}
                        </span>
                      </td>
                      <td>
                        <strong style={{ color: "var(--green)" }}>{q.options[q.correctOptionIndex]}</strong>
                      </td>
                      <td>
                        <small>{q.explanation}</small>
                      </td>
                      <td>
                        <span className={`status-label ${q.status === "ACTIVE" ? "" : "cancelled"}`}>
                          {q.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ================= 10. EXPERT ASSIGNED SUBJECTS ================= */}
        {navKey === "expert_subjects" && (
          <div>
            <div className="workspace-heading">
              <div>
                <div className="eyebrow">
                  <span className="eyebrow-line"></span>
                  <span>Chuyên môn phân công</span>
                </div>
                <h1>Môn Học Được Phân Công (Assigned Subjects)</h1>
              </div>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "20px" }}>
              {subjects.map((s) => (
                <div key={s.id} className="workspace-panel">
                  <span className="art-category" style={{ position: "static", display: "inline-block", marginBottom: "8px" }}>
                    {s.category}
                  </span>
                  <h3 style={{ fontSize: "20px", margin: "4px 0 8px" }}>{s.name}</h3>
                  <p style={{ color: "var(--muted)", fontSize: "13px", lineHeight: "1.6" }}>{s.description}</p>
                  <div style={{ marginTop: "14px", borderTop: "1px solid #edf0ea", paddingTop: "12px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <span style={{ fontSize: "11px", color: "var(--muted)" }}>
                      Giảng viên: <strong>{s.expertName}</strong>
                    </span>
                    <button onClick={() => setNavKey("expert_questions")} className="button button-small button-dark">
                      Quản lý câu hỏi & đề thi →
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </main>

      {/* ================= MODAL: ADD SUBJECT ================= */}
      {showAddSubjectModal && (
        <div className="dialog-overlay" onClick={() => setShowAddSubjectModal(false)}>
          <div className="dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "500px" }}>
            <button className="dialog-close" onClick={() => setShowAddSubjectModal(false)}>✕</button>
            <h2 style={{ fontSize: "22px", marginBottom: "16px" }}>Tạo Môn Học Mới</h2>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                const fd = new FormData(e.currentTarget);
                const newSub: Subject = {
                  id: Date.now(),
                  name: String(fd.get("name")),
                  category: String(fd.get("category")),
                  expertName: String(fd.get("expertName")),
                  description: String(fd.get("desc")),
                  active: true,
                };
                setSubjects([...subjects, newSub]);
                setShowAddSubjectModal(false);
                setNotice(`Đã tạo môn học mới: "${newSub.name}"!`);
              }}
              className="workspace-form"
            >
              <div>
                <label>TÊN MÔN HỌC *</label>
                <input type="text" name="name" required placeholder="Ví dụ: Lập trình Trí tuệ Nhân tạo" />
              </div>
              <div className="form-row">
                <div>
                  <label>CHUYÊN NGÀNH</label>
                  <select name="category">
                    <option value="Development">Development</option>
                    <option value="Design">Design</option>
                    <option value="Business">Business</option>
                    <option value="Creative">Creative</option>
                  </select>
                </div>
                <div>
                  <label>GIẢNG VIÊN PHỤ TRÁCH (EXPERT)</label>
                  <select name="expertName">
                    <option value="Alex Tran">Alex Tran</option>
                    <option value="Maya Nguyen">Maya Nguyen</option>
                    <option value="Jordan Lee">Jordan Lee</option>
                    <option value="Sam Rivera">Sam Rivera</option>
                  </select>
                </div>
              </div>
              <div>
                <label>MÔ TẢ MÔN HỌC</label>
                <textarea name="desc" rows={3} placeholder="Mô tả nội dung môn học..." />
              </div>
              <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
                <button type="button" onClick={() => setShowAddSubjectModal(false)} className="text-button">
                  Hủy
                </button>
                <button type="submit" className="button button-dark">
                  Tạo Môn Học
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ================= MODAL: ADD USER ================= */}
      {showAddUserModal && (
        <div className="dialog-overlay" onClick={() => setShowAddUserModal(false)}>
          <div className="dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "500px" }}>
            <button className="dialog-close" onClick={() => setShowAddUserModal(false)}>✕</button>
            <h2 style={{ fontSize: "22px", marginBottom: "16px" }}>Thêm Người Dùng Mới</h2>
            <form
              onSubmit={(e) => {
                e.preventDefault();
                const fd = new FormData(e.currentTarget);
                const newUser: AdminUser = {
                  id: Date.now(),
                  fullName: String(fd.get("fullName")),
                  email: String(fd.get("email")),
                  phone: String(fd.get("phone")),
                  gender: String(fd.get("gender")),
                  role: String(fd.get("role")),
                  status: "ACTIVE",
                  createdAt: new Date().toISOString().split("T")[0],
                };
                setUsers([...users, newUser]);
                setShowAddUserModal(false);
                setNotice(`Đã tạo tài khoản cho: ${newUser.fullName} (${newUser.role})!`);
              }}
              className="workspace-form"
            >
              <div>
                <label>HỌ VÀ TÊN *</label>
                <input type="text" name="fullName" required placeholder="Nguyễn Văn B" />
              </div>
              <div className="form-row">
                <div>
                  <label>ĐỊA CHỈ EMAIL *</label>
                  <input type="email" name="email" required placeholder="email@example.com" />
                </div>
                <div>
                  <label>SỐ ĐIỆN THOẠI</label>
                  <input type="tel" name="phone" placeholder="0912 345 678" />
                </div>
              </div>
              <div className="form-row">
                <div>
                  <label>GIỚI TÍNH</label>
                  <select name="gender">
                    <option value="Nam">Nam</option>
                    <option value="Nữ">Nữ</option>
                    <option value="Khác">Khác</option>
                  </select>
                </div>
                <div>
                  <label>VAI TRÒ (ROLE) *</label>
                  <select name="role">
                    <option value="CUSTOMER">CUSTOMER (Học viên)</option>
                    <option value="SALE">SALE (Bán hàng)</option>
                    <option value="MARKETING">MARKETING (Tiếp thị)</option>
                    <option value="EXPERT">EXPERT (Giảng viên)</option>
                    <option value="ADMIN">ADMIN (Quản trị viên)</option>
                  </select>
                </div>
              </div>
              <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px", marginTop: "10px" }}>
                <button type="button" onClick={() => setShowAddUserModal(false)} className="text-button">
                  Hủy
                </button>
                <button type="submit" className="button button-dark">
                  Tạo Tài Khoản
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}