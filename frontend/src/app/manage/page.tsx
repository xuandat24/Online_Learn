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
  const [user, setUser] = useState<SessionUser | null>(null);
  const [isLoaded, setIsLoaded] = useState(false);

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
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [profileTab, setProfileTab] = useState<"info" | "password">("info");

  // Notifications
  const [notice, setNotice] = useState("");

  // Restore user session
  useEffect(() => {
    try {
      const storedToken = sessionStorage.getItem("morrow.token") || localStorage.getItem("morrow.token");
      const storedUser = sessionStorage.getItem("morrow.user") || localStorage.getItem("morrow.user");
      if (storedUser) {
        const parsed = JSON.parse(storedUser) as SessionUser;
        setUser(parsed);
        if (storedToken) setToken(storedToken);

        // Auto select first allowed tab according to role
        if (parsed.role === "SALE") setNavKey("sale_dashboard");
        else if (parsed.role === "MARKETING") setNavKey("mkt_dashboard");
        else if (parsed.role === "EXPERT") setNavKey("expert_subjects");
        else setNavKey("admin_dashboard");
      }
    } catch {}
    setIsLoaded(true);
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

  // Sign out
  const signOut = () => {
    sessionStorage.removeItem("morrow.token");
    sessionStorage.removeItem("morrow.user");
    localStorage.removeItem("morrow.token");
    localStorage.removeItem("morrow.user");
    setUser(null);
    window.location.href = "/";
  };

  // Quick Login for Internal Staff
  const quickLoginInternal = async (email: string, roleName: "ADMIN" | "SALE" | "MARKETING" | "EXPERT") => {
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password: "password123" }),
      });
      if (res.ok) {
        const data = await res.json();
        sessionStorage.setItem("morrow.token", data.token);
        sessionStorage.setItem("morrow.user", JSON.stringify(data.user));
        localStorage.setItem("morrow.token", data.token);
        localStorage.setItem("morrow.user", JSON.stringify(data.user));
        setUser(data.user);
        setToken(data.token);
        if (roleName === "SALE") setNavKey("sale_dashboard");
        else if (roleName === "MARKETING") setNavKey("mkt_dashboard");
        else if (roleName === "EXPERT") setNavKey("expert_subjects");
        else setNavKey("admin_dashboard");
        setNotice(`Đăng nhập thành công với vai trò: ${roleName}`);
        return;
      }
    } catch {}

    const fallback: SessionUser = { id: 1, fullName: `Demo ${roleName}`, email, role: roleName };
    sessionStorage.setItem("morrow.token", `demo-token-${roleName.toLowerCase()}`);
    sessionStorage.setItem("morrow.user", JSON.stringify(fallback));
    localStorage.setItem("morrow.token", `demo-token-${roleName.toLowerCase()}`);
    localStorage.setItem("morrow.user", JSON.stringify(fallback));
    setUser(fallback);
    if (roleName === "SALE") setNavKey("sale_dashboard");
    else if (roleName === "MARKETING") setNavKey("mkt_dashboard");
    else if (roleName === "EXPERT") setNavKey("expert_subjects");
    else setNavKey("admin_dashboard");
    setNotice(`Đã chuyển đổi sang tài khoản: ${roleName}`);
  };

  // Switch role test shortcut
  const switchRole = (newRole: "ADMIN" | "SALE" | "MARKETING" | "EXPERT") => {
    if (!user) return;
    const updated: SessionUser = { ...user, role: newRole, fullName: `Demo ${newRole}` };
    setUser(updated);
    sessionStorage.setItem("morrow.user", JSON.stringify(updated));
    localStorage.setItem("morrow.user", JSON.stringify(updated));
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

  // Loading state check
  if (!isLoaded) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh", background: "#f8fafc" }}>
        <div style={{ textAlign: "center" }}>
          <div className="brand-mark" style={{ width: "44px", height: "44px", fontSize: "24px", margin: "0 auto 12px" }}>O</div>
          <p style={{ color: "#64748b", fontSize: "14px", fontWeight: 500 }}>Đang kiểm tra quyền truy cập hệ thống...</p>
        </div>
      </div>
    );
  }

  // 401 Unauthenticated: User is not logged in
  if (!user) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh", background: "#0f172a", padding: "20px" }}>
        <div style={{ background: "#ffffff", borderRadius: "16px", padding: "36px", maxWidth: "480px", width: "100%", boxShadow: "0 20px 40px rgba(0,0,0,0.3)", textAlign: "center" }}>
          <div style={{ width: "56px", height: "56px", borderRadius: "50%", background: "#fef3c7", color: "#d97706", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "26px", margin: "0 auto 16px" }}>
            🔒
          </div>
          <h2 style={{ fontSize: "22px", color: "#0f172a", marginBottom: "8px", fontWeight: 700 }}>
            Yêu cầu Đăng nhập Quản trị
          </h2>
          <p style={{ fontSize: "13px", color: "#64748b", lineHeight: "1.6", marginBottom: "24px" }}>
            Cổng này chỉ dành riêng cho Ban Quản trị & Nhân viên nội bộ (Admin, Sale, Expert, Marketing). Vui lòng đăng nhập để tiếp tục.
          </p>

          <div style={{ background: "#f8fafc", border: "1px solid #e2e8f0", padding: "16px", borderRadius: "12px", marginBottom: "20px", textAlign: "left" }}>
            <span style={{ fontSize: "11px", fontWeight: 700, color: "#475569", display: "block", marginBottom: "10px" }}>
              ⚡ ĐĂNG NHẬP NHANH THEO VAI TRÒ NỘI BỘ:
            </span>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "8px" }}>
              <button
                type="button"
                onClick={() => quickLoginInternal("admin@example.com", "ADMIN")}
                className="demo-role-card"
              >
                <span className="role-badge role-admin" style={{ fontSize: "9px", padding: "2px 6px" }}>👑 ADMIN</span>
                <strong>Quản trị viên</strong>
              </button>
              <button
                type="button"
                onClick={() => quickLoginInternal("sale@example.com", "SALE")}
                className="demo-role-card"
              >
                <span className="role-badge role-sale" style={{ fontSize: "9px", padding: "2px 6px" }}>💼 SALE</span>
                <strong>Tư vấn Sale</strong>
              </button>
              <button
                type="button"
                onClick={() => quickLoginInternal("expert@example.com", "EXPERT")}
                className="demo-role-card"
              >
                <span className="role-badge role-expert" style={{ fontSize: "9px", padding: "2px 6px" }}>🔬 EXPERT</span>
                <strong>Chuyên gia</strong>
              </button>
              <button
                type="button"
                onClick={() => quickLoginInternal("marketing@example.com", "MARKETING")}
                className="demo-role-card"
              >
                <span className="role-badge role-marketing" style={{ fontSize: "9px", padding: "2px 6px" }}>📢 MARKETING</span>
                <strong>Tiếp thị</strong>
              </button>
            </div>
          </div>

          <Link href="/" className="button button-dark" style={{ width: "100%", display: "inline-block", textDecoration: "none", padding: "12px" }}>
            ← Quay về Trang chủ
          </Link>
        </div>
      </div>
    );
  }

  // 403 Forbidden: User is logged in as CUSTOMER
  if (user.role === "CUSTOMER") {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh", background: "#f8fafc", padding: "20px" }}>
        <div style={{ background: "#ffffff", border: "1px solid #fee2e2", borderRadius: "16px", padding: "36px", maxWidth: "500px", width: "100%", boxShadow: "0 12px 30px rgba(0,0,0,0.06)", textAlign: "center" }}>
          <div style={{ width: "64px", height: "64px", borderRadius: "50%", background: "#fee2e2", color: "#dc2626", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "30px", margin: "0 auto 16px" }}>
            🚫
          </div>
          <span className="role-badge role-customer" style={{ marginBottom: "10px" }}>🎓 TÀI KHOẢN HỌC VIÊN</span>
          <h2 style={{ fontSize: "22px", color: "#991b1b", margin: "10px 0 8px", fontWeight: 700 }}>
            403 - Quyền Truy Cập Bị Từ Chối
          </h2>
          <p style={{ fontSize: "13.5px", color: "#475569", lineHeight: "1.6", marginBottom: "24px" }}>
            Xin chào <strong>{user.fullName}</strong>. Tài khoản của bạn được cấp quyền <strong>Học viên (CUSTOMER)</strong>, không thể truy cập Cổng Quản trị nội bộ dành cho nhân viên và quản trị viên.
          </p>

          <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
            <Link href="/learn" className="button button-accent" style={{ padding: "12px", textDecoration: "none", fontWeight: 600 }}>
              📚 Chuyển sang Cổng Học viên của bạn
            </Link>
            <Link href="/" className="button" style={{ padding: "12px", background: "#f1f5f9", color: "#334155", textDecoration: "none", fontWeight: 600 }}>
              🏠 Về Trang chủ Website
            </Link>
            <button
              onClick={signOut}
              className="text-button"
              style={{ color: "#dc2626", marginTop: "6px", fontSize: "13px" }}
            >
              🚪 Đăng xuất để đăng nhập tài khoản Quản trị
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-shell">
      {/* ================= ADMIN SIDEBAR ================= */}
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span className="brand-mark" style={{ width: "26px", height: "26px", fontSize: "18px" }}>O</span>
          <span>OnlineLearn</span>
        </div>

        {/* User Identity in Sidebar - Clickable to open Profile & Password Modal */}
        <div
          onClick={() => { setShowProfileModal(true); setProfileTab("info"); }}
          title="Nhấp để xem Hồ sơ cá nhân & Đổi mật khẩu"
          style={{
            padding: "8px 10px 14px",
            borderBottom: "1px solid rgba(255,255,255,0.08)",
            marginBottom: "18px",
            cursor: "pointer",
            borderRadius: "8px",
            background: "rgba(255,255,255,0.04)",
            transition: "all 0.2s",
          }}
        >
          <strong style={{ display: "flex", alignItems: "center", justifyContent: "space-between", color: "#fff", fontSize: "13.5px", marginBottom: "4px" }}>
            <span>{user.fullName}</span>
            <span style={{ fontSize: "10px", color: "#94a3b8" }}>⚙️</span>
          </strong>
          <div>
            {user.role === "ADMIN" && <span className="role-badge role-admin" style={{ fontSize: "9.5px", padding: "2px 8px" }}>👑 QUẢN TRỊ VIÊN</span>}
            {user.role === "SALE" && <span className="role-badge role-sale" style={{ fontSize: "9.5px", padding: "2px 8px" }}>💼 TƯ VẤN SALE</span>}
            {user.role === "EXPERT" && <span className="role-badge role-expert" style={{ fontSize: "9.5px", padding: "2px 8px" }}>🔬 CHUYÊN GIA</span>}
            {user.role === "MARKETING" && <span className="role-badge role-marketing" style={{ fontSize: "9.5px", padding: "2px 8px" }}>📢 MARKETING</span>}
          </div>
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
        <div style={{ borderTop: "1px solid rgba(255,255,255,0.1)", paddingTop: "14px", marginTop: "auto" }}>
          <span style={{ fontSize: "10px", color: "#8da394", display: "block", marginBottom: "8px", fontWeight: 600 }}>
            ⚡ CHUYỂN VAI TRÒ KIỂM THỬ:
          </span>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "6px" }}>
            <button
              type="button"
              onClick={() => switchRole("ADMIN")}
              style={{
                background: user.role === "ADMIN" ? "rgba(255,255,255,0.2)" : "rgba(255,255,255,0.06)",
                border: "1px solid rgba(255,255,255,0.15)",
                color: "#ffffff",
                padding: "6px 8px",
                borderRadius: "6px",
                fontSize: "10.5px",
                cursor: "pointer",
                fontWeight: user.role === "ADMIN" ? 700 : 400,
              }}
            >
              👑 Admin
            </button>
            <button
              type="button"
              onClick={() => switchRole("SALE")}
              style={{
                background: user.role === "SALE" ? "rgba(255,255,255,0.2)" : "rgba(255,255,255,0.06)",
                border: "1px solid rgba(255,255,255,0.15)",
                color: "#ffffff",
                padding: "6px 8px",
                borderRadius: "6px",
                fontSize: "10.5px",
                cursor: "pointer",
                fontWeight: user.role === "SALE" ? 700 : 400,
              }}
            >
              💼 Sale
            </button>
            <button
              type="button"
              onClick={() => switchRole("MARKETING")}
              style={{
                background: user.role === "MARKETING" ? "rgba(255,255,255,0.2)" : "rgba(255,255,255,0.06)",
                border: "1px solid rgba(255,255,255,0.15)",
                color: "#ffffff",
                padding: "6px 8px",
                borderRadius: "6px",
                fontSize: "10.5px",
                cursor: "pointer",
                fontWeight: user.role === "MARKETING" ? 700 : 400,
              }}
            >
              📢 Mkt
            </button>
            <button
              type="button"
              onClick={() => switchRole("EXPERT")}
              style={{
                background: user.role === "EXPERT" ? "rgba(255,255,255,0.2)" : "rgba(255,255,255,0.06)",
                border: "1px solid rgba(255,255,255,0.15)",
                color: "#ffffff",
                padding: "6px 8px",
                borderRadius: "6px",
                fontSize: "10.5px",
                cursor: "pointer",
                fontWeight: user.role === "EXPERT" ? 700 : 400,
              }}
            >
              🔬 Expert
            </button>
          </div>

          <div style={{ marginTop: "14px", display: "flex", flexDirection: "column", gap: "6px" }}>
            <Link
              href="/"
              style={{
                color: "#cbd5e1",
                fontSize: "11.5px",
                textDecoration: "none",
                display: "flex",
                alignItems: "center",
                gap: "6px",
                padding: "5px 0",
              }}
            >
              🏠 Quay về Trang chủ
            </Link>
            <button
              type="button"
              onClick={signOut}
              style={{
                background: "rgba(239, 68, 68, 0.15)",
                border: "1px solid rgba(239, 68, 68, 0.3)",
                color: "#fca5a5",
                fontSize: "11.5px",
                fontWeight: 600,
                cursor: "pointer",
                borderRadius: "6px",
                padding: "6px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: "6px",
              }}
            >
              🚪 Đăng xuất hệ thống
            </button>
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

      {/* ================= MODAL: USER PROFILE & CHANGE PASSWORD ================= */}
      {showProfileModal && user && (
        <div className="dialog-overlay" onClick={() => setShowProfileModal(false)}>
          <div className="dialog auth-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "540px", borderRadius: "20px" }}>
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
                  const updated: SessionUser = {
                    ...user,
                    fullName: String(fd.get("fullName")),
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
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)", display: "block", marginBottom: "4px" }}>HỌ VÀ TÊN *</label>
                  <input type="text" name="fullName" defaultValue={user.fullName} required style={{ width: "100%", padding: "10px 12px", border: "1px solid var(--line)", borderRadius: "8px", fontSize: "13px" }} />
                </div>

                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)", display: "block", marginBottom: "4px" }}>ĐỊA CHỈ EMAIL</label>
                  <input type="email" value={user.email} disabled style={{ width: "100%", padding: "10px 12px", border: "1px solid var(--line)", borderRadius: "8px", background: "#f8fafc", color: "#64748b", fontSize: "13px" }} />
                </div>

                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)", display: "block", marginBottom: "4px" }}>VAI TRÒ QUẢN TRỊ</label>
                  <input type="text" value={user.role} disabled style={{ width: "100%", padding: "10px 12px", border: "1px solid var(--line)", borderRadius: "8px", background: "#f8fafc", color: "#64748b", fontSize: "13px" }} />
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
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)", display: "block", marginBottom: "4px" }}>MẬT KHẨU HIỆN TẠI *</label>
                  <input type="password" name="oldPass" required placeholder="••••••••" style={{ width: "100%", padding: "10px 12px", border: "1px solid var(--line)", borderRadius: "8px", fontSize: "13px" }} />
                </div>
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)", display: "block", marginBottom: "4px" }}>MẬT KHẨU MỚI *</label>
                  <input type="password" name="newPass" required placeholder="Tối thiểu 8 ký tự" style={{ width: "100%", padding: "10px 12px", border: "1px solid var(--line)", borderRadius: "8px", fontSize: "13px" }} />
                </div>
                <div>
                  <label style={{ fontSize: "11px", fontWeight: 700, color: "var(--ink)", display: "block", marginBottom: "4px" }}>XÁC NHẬN MẬT KHẨU MỚI *</label>
                  <input type="password" name="confirmPass" required placeholder="Nhập lại mật khẩu mới" style={{ width: "100%", padding: "10px 12px", border: "1px solid var(--line)", borderRadius: "8px", fontSize: "13px" }} />
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