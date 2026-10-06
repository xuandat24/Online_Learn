"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";

type Course = {
  id: number;
  title: string;
  description: string;
  category: string;
  instructor: string;
  level: string;
  duration: string;
  price: number;
  rating: number;
  students: number;
  image: string;
  accent: string;
  subjectId?: number | null;
  subjectName?: string | null;
  pricePackages?: PricePackage[];
};

type PricePackage = {
  id: number;
  name: string;
  price: number;
  currency: string;
  accessDays: number;
  published: boolean;
};

type AuthUser = {
  id: number;
  fullName: string;
  email: string;
  role: string;
  gender?: string;
  mobile?: string;
};

type BlogPost = {
  id: number;
  title: string;
  category: string;
  author: string;
  updatedAt: string;
  summary: string;
  content: string;
  image: string;
};

const sampleSliders = [
  {
    id: 1,
    tag: "Khóa học nổi bật",
    title: "Làm chủ Kỹ năng Số & Bứt phá Sự nghiệp 2026",
    description: "Học tập tương tác cùng các chuyên gia hàng đầu. Hệ thống bài giảng cập nhật liên tục với thực tiễn doanh nghiệp.",
    image: "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80",
    cta: "Khám phá khóa học",
  },
  {
    id: 2,
    tag: "Học phí ưu đãi",
    title: "Đăng ký Gói Trọn đời - Tiết kiệm đến 40%",
    description: "Truy cập không giới hạn kho bài giảng, tham gia các buổi review đồ án trực tiếp và nhận hỗ trợ 1-1 từ giảng viên.",
    image: "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80",
    cta: "Xem gói học",
  },
  {
    id: 3,
    tag: "Đánh giá năng lực",
    title: "Hệ thống Thi Trắc nghiệm & Cấp Chứng chỉ",
    description: "Luyện tập với ngân hàng câu hỏi đa cấp độ, thi thử với đồng hồ đếm ngược và xem giải thích đáp án chi tiết.",
    image: "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=1200&q=80",
    cta: "Luyện thi ngay",
  },
];

const sampleSubjects = [
  { id: 1, name: "Phát triển Phần mềm", icon: "💻", category: "Development", count: 14, desc: "Frontend, Backend, Database và Kiến trúc ứng dụng hiện đại" },
  { id: 2, name: "Thiết kế UI/UX & Đồ họa", icon: "🎨", category: "Design", count: 8, desc: "Design System, Figma, Wireframe và Trải nghiệm người dùng" },
  { id: 3, name: "Kinh doanh & Phân tích", icon: "📈", category: "Business", count: 11, desc: "SQL cho phân tích, Marketing số và Xây dựng thương hiệu" },
  { id: 4, name: "Sáng tạo & Viết lách", icon: "✍️", category: "Creative", count: 6, desc: "Kỹ năng viết nội dung, Nhiếp ảnh ánh sáng tự nhiên và Kể chuyện" },
];

const sampleBlogs: BlogPost[] = [
  {
    id: 1,
    title: "Lộ trình tự học Frontend từ số 0 đến khi có việc làm",
    category: "Development",
    author: "Alex Tran",
    updatedAt: "01/10/2026",
    summary: "Những nguyên lý cốt lõi cần nắm vững về HTML/CSS/JS, React và Next.js thay vì học thuộc lòng framework.",
    content: "Để bắt đầu sự nghiệp Frontend vững chắc, bạn cần hiểu rõ cách trình duyệt render HTML, cách CSS cascade hoạt động và JavaScript bất đồng bộ. Đừng vội lao vào học các thư viện phức tạp trước khi tự mình làm được một trang web tương tác cơ bản bằng mã nguồn thuần. Dự án thực tế này sẽ giúp bạn hiểu sâu sắc kiến trúc từng thành phần.",
    image: "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: 2,
    title: "5 nguyên tắc thiết kế Design System cho sản phẩm thực tế",
    category: "Design",
    author: "Maya Nguyen",
    updatedAt: "28/09/2026",
    summary: "Làm thế nào để tạo ra một ngôn ngữ thị giác nhất quán mà lập trình viên và designer đều yêu thích sử dụng.",
    content: "Design system không chỉ là một bảng màu hay thư viện component Figma. Nó là cầu nối giao tiếp giữa thiết kế và lập trình. Bạn cần xác định rõ các Design Tokens (khoảng cách, màu sắc, font chữ), quy tắc đặt tên thống nhất và khả năng mở rộng khi dự án tăng trưởng.",
    image: "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=800&q=80",
  },
  {
    id: 3,
    title: "Ứng dụng SQL trong phân tích dữ liệu kinh doanh hàng ngày",
    category: "Business",
    author: "Jordan Lee",
    updatedAt: "25/09/2026",
    summary: "Khám phá các truy vấn thực tế giúp bạn chuyển đổi dữ liệu thô thành quyết định chiến lược hiệu quả.",
    content: "Dữ liệu kinh doanh thường nằm rải rác ở nhiều bảng. Việc thành thạo các câu lệnh JOIN, GROUP BY và Window Functions sẽ giúp bạn tự tin tính toán doanh thu, tỷ lệ giữ chân khách hàng (retention rate) và xu hướng đơn hàng một cách nhanh chóng mà không phụ thuộc vào đội ngũ kỹ thuật.",
    image: "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80",
  },
];

const initialCourses: Course[] = [
  {
    id: 1,
    title: "Design systems that scale",
    description: "Build a thoughtful visual language, reusable components, and a system your whole team can trust.",
    category: "Design",
    instructor: "Maya Nguyen",
    level: "Intermediate",
    duration: "6h 20m",
    price: 39,
    rating: 4.9,
    students: 1240,
    image: "photo-1498050108023-c5249f4df085",
    accent: "sage",
    subjectName: "Thiết kế UI/UX & Đồ họa",
    pricePackages: [
      { id: 101, name: "Gói tiêu chuẩn (6 tháng)", price: 39, currency: "USD", accessDays: 180, published: true },
      { id: 102, name: "Gói trọn đời (Vĩnh viễn)", price: 69, currency: "USD", accessDays: 0, published: true },
    ],
  },
  {
    id: 2,
    title: "The curious data analyst",
    description: "Turn messy data into clear decisions with practical SQL, visual storytelling, and real projects.",
    category: "Business",
    instructor: "Jordan Lee",
    level: "Beginner",
    duration: "8h 10m",
    price: 49,
    rating: 4.8,
    students: 2180,
    image: "photo-1460925895917-afdab827c52f",
    accent: "blue",
    subjectName: "Kinh doanh & Phân tích",
    pricePackages: [
      { id: 201, name: "Gói tiêu chuẩn (6 tháng)", price: 49, currency: "USD", accessDays: 180, published: true },
      { id: 202, name: "Gói trọn đời (Vĩnh viễn)", price: 89, currency: "USD", accessDays: 0, published: true },
    ],
  },
  {
    id: 3,
    title: "Frontend, from first principles",
    description: "A hands-on path through modern web interfaces, from the first component to a polished product.",
    category: "Development",
    instructor: "Alex Tran",
    level: "Beginner",
    duration: "12h 40m",
    price: 59,
    rating: 5.0,
    students: 3560,
    image: "photo-1516321318423-f06f85e504b3",
    accent: "peach",
    subjectName: "Phát triển Phần mềm",
    pricePackages: [
      { id: 301, name: "Gói cơ bản (3 tháng)", price: 59, currency: "USD", accessDays: 90, published: true },
      { id: 302, name: "Gói trọn đời (Vĩnh viễn)", price: 99, currency: "USD", accessDays: 0, published: true },
    ],
  },
  {
    id: 4,
    title: "Make space for better writing",
    description: "Find your voice, shape a strong narrative, and edit your words until every sentence earns its place.",
    category: "Creative",
    instructor: "Sam Rivera",
    level: "All levels",
    duration: "4h 35m",
    price: 29,
    rating: 4.9,
    students: 890,
    image: "photo-1455390582262-044cdead277a",
    accent: "yellow",
    subjectName: "Sáng tạo & Viết lách",
    pricePackages: [
      { id: 401, name: "Gói học trọn gói", price: 29, currency: "USD", accessDays: 0, published: true },
    ],
  },
  {
    id: 5,
    title: "Small business, stronger brand",
    description: "Make clear positioning and useful marketing plans for the business you are building today.",
    category: "Business",
    instructor: "Priya Shah",
    level: "Intermediate",
    duration: "5h 50m",
    price: 44,
    rating: 4.7,
    students: 1620,
    image: "photo-1454165804606-c3d57bc86b40",
    accent: "rose",
    subjectName: "Kinh doanh & Phân tích",
    pricePackages: [
      { id: 501, name: "Gói tiêu chuẩn", price: 44, currency: "USD", accessDays: 180, published: true },
    ],
  },
  {
    id: 6,
    title: "Photography in natural light",
    description: "Learn to notice, shape, and capture the light that makes an ordinary moment feel alive.",
    category: "Creative",
    instructor: "Linh Pham",
    level: "All levels",
    duration: "7h 05m",
    price: 35,
    rating: 4.9,
    students: 970,
    image: "photo-1452587925148-ce544e77e70d",
    accent: "lavender",
    subjectName: "Sáng tạo & Viết lách",
    pricePackages: [
      { id: 601, name: "Gói học trọn gói", price: 35, currency: "USD", accessDays: 0, published: true },
    ],
  },
];

const categories = ["Tất cả", "Development", "Design", "Business", "Creative"];

export default function GuestHome() {
  const [courses, setCourses] = useState<Course[]>(initialCourses);
  const [activeCategory, setActiveCategory] = useState("Tất cả");
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 3;

  // Slider State
  const [activeSlide, setActiveSlide] = useState(0);

  // Modals & Selected items
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [registerCourse, setRegisterCourse] = useState<Course | null>(null);
  const [selectedPackageId, setSelectedPackageId] = useState<number | null>(null);
  const [selectedBlog, setSelectedBlog] = useState<BlogPost | null>(null);

  // Auth State
  const [authMode, setAuthMode] = useState<"login" | "register" | "forgot" | null>(null);
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);

  // Auto-advance slider
  useEffect(() => {
    const timer = setInterval(() => {
      setActiveSlide((prev) => (prev + 1) % sampleSliders.length);
    }, 6000);
    return () => clearInterval(timer);
  }, []);

  // Restore session
  useEffect(() => {
    const storedToken = sessionStorage.getItem("morrow.token");
    const storedUser = sessionStorage.getItem("morrow.user");
    if (storedToken && storedUser) {
      try {
        const parsed = JSON.parse(storedUser) as AuthUser;
        setToken(storedToken);
        setUser(parsed);
      } catch {
        sessionStorage.removeItem("morrow.token");
        sessionStorage.removeItem("morrow.user");
      }
    }
  }, []);

  // Fetch real courses from API if available
  useEffect(() => {
    fetch("/api/courses")
      .then((res) => (res.ok ? res.json() : Promise.reject()))
      .then((data: Course[]) => {
        if (Array.isArray(data) && data.length > 0) {
          setCourses(data);
        }
      })
      .catch(() => {
        // Fallback to initial seed courses
      });
  }, []);

  // Filtered Courses
  const filteredCourses = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return courses.filter((c) => {
      const matchCat = activeCategory === "Tất cả" || c.category === activeCategory;
      const matchSearch =
        !q || `${c.title} ${c.description} ${c.instructor} ${c.category}`.toLowerCase().includes(q);
      return matchCat && matchSearch;
    });
  }, [activeCategory, courses, searchQuery]);

  const totalPages = Math.ceil(filteredCourses.length / itemsPerPage) || 1;
  const paginatedCourses = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    return filteredCourses.slice(start, start + itemsPerPage);
  }, [currentPage, filteredCourses]);

  // Handle Guest Course Registration
  async function submitRegistration(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!registerCourse || !selectedPackageId) return;
    const form = new FormData(e.currentTarget);
    const payload = {
      courseId: registerCourse.id,
      pricePackageId: selectedPackageId,
      fullName: String(form.get("fullName")),
      gender: String(form.get("gender") || "Male"),
      email: String(form.get("email")),
      phone: String(form.get("phone")),
    };

    setBusy(true);
    setNotice("");
    try {
      const response = await fetch("/api/registrations", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(payload),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Gửi đăng ký thất bại.");
      setNotice(
        `Đăng ký thành công khóa "${registerCourse.title}"! Đơn ở trạng thái SUBMITTED. Nhân viên Sale sẽ liên hệ xác nhận thanh toán và gửi thông tin tài khoản cho bạn.`
      );
      setRegisterCourse(null);
      setSelectedCourse(null);
    } catch (err) {
      setNotice(err instanceof Error ? err.message : "Có lỗi xảy ra, vui lòng thử lại.");
    } finally {
      setBusy(false);
    }
  }

  // Handle Auth
  async function submitAuth(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!authMode) return;
    const form = new FormData(e.currentTarget);

    if (authMode === "forgot") {
      setNotice(`Link đặt lại mật khẩu đã được gửi đến email ${form.get("email")}. Vui lòng kiểm tra hộp thư!`);
      setAuthMode(null);
      return;
    }

    const payload: Record<string, string> = {
      email: String(form.get("email")),
      password: String(form.get("password")),
    };
    if (authMode === "register") {
      payload.fullName = String(form.get("fullName"));
      payload.phone = String(form.get("phone"));
    }

    setBusy(true);
    setNotice("");
    try {
      const response = await fetch(`/api/auth/${authMode}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "Đăng nhập/Đăng ký không thành công.");
      setToken(data.token);
      setUser(data.user);
      sessionStorage.setItem("morrow.token", data.token);
      sessionStorage.setItem("morrow.user", JSON.stringify(data.user));
      setAuthMode(null);
      setNotice(`Xin chào, ${data.user?.fullName}! Vai trò của bạn: [${data.user?.role}].`);
    } catch (err) {
      setNotice(err instanceof Error ? err.message : "Xác thực thất bại.");
    } finally {
      setBusy(false);
    }
  }

  function demoLogin(email: string, roleName: string) {
    const demoUser: AuthUser = {
      id: 99,
      fullName: `Demo ${roleName}`,
      email: email,
      role: roleName,
    };
    setUser(demoUser);
    setToken("demo-token-bypass");
    sessionStorage.setItem("morrow.token", "demo-token-bypass");
    sessionStorage.setItem("morrow.user", JSON.stringify(demoUser));
    setAuthMode(null);
    setNotice(`Đã chuyển đổi sang tài khoản Demo với vai trò: ${roleName}`);
  }

  function signOut() {
    setUser(null);
    setToken("");
    sessionStorage.removeItem("morrow.token");
    sessionStorage.removeItem("morrow.user");
    setNotice("Đã đăng xuất khỏi hệ thống.");
  }

  return (
    <div className="site-shell">
      {/* ================= ACTOR SWITCHER BAR ================= */}
      <div className="actor-banner">
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <span className="actor-badge">👤 Actor: GUEST (Cổng Khách & Công Khai)</span>
          <span style={{ opacity: 0.8 }}>Khám phá khóa học, xem tin tức và đăng ký học</span>
        </div>
        <div className="actor-links">
          <span>Chuyển nhanh sang:</span>
          <Link href="/learn" className="actor-btn">
            🎓 Giao diện Học viên (Customer)
          </Link>
          <Link href="/manage" className="actor-btn">
            ⚙️ Cổng Quản trị (Admin / Sale / Expert / Mkt)
          </Link>
        </div>
      </div>

      {/* ================= TOPBAR ================= */}
      <header className="topbar">
        <Link href="/" className="brand">
          <span className="brand-mark">O</span>
          <span>OnlineLearn</span>
          <span className="brand-dot">.</span>
        </Link>

        <nav className="main-nav">
          <a href="#home" className="nav-active">Trang chủ</a>
          <a href="#courses">Khóa học</a>
          <a href="#subjects">Chủ đề</a>
          <a href="#blogs">Tin tức & Bài viết</a>
          <a href="#footer">Liên hệ</a>
        </nav>

        <div className="account-actions">
          {user ? (
            <div style={{ display: "flex", alignItems: "center", gap: "14px" }}>
              <span className="user-greeting">
                Chào, <strong>{user.fullName}</strong> ({user.role})
              </span>
              {user.role === "CUSTOMER" && (
                <Link href="/learn" className="button button-small button-accent">
                  Khu vực học tập
                </Link>
              )}
              {["ADMIN", "SALE", "MARKETING", "EXPERT"].includes(user.role) && (
                <Link href="/manage" className="button button-small button-dark">
                  Trang quản trị
                </Link>
              )}
              <button onClick={signOut} className="text-button">
                Đăng xuất
              </button>
            </div>
          ) : (
            <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
              <button onClick={() => setAuthMode("login")} className="text-button">
                Đăng nhập
              </button>
              <button onClick={() => setAuthMode("register")} className="button button-small button-dark">
                Đăng ký thành viên
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Notification Banner */}
      {notice && (
        <div style={{ background: "#eef5ea", borderBottom: "1px solid #c9dec2", padding: "12px 6.1%", color: "#284a37", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span>🔔 {notice}</span>
          <button onClick={() => setNotice("")} style={{ border: 0, background: "none", cursor: "pointer", fontWeight: 700 }}>✕</button>
        </div>
      )}

      {/* ================= 1. HERO SLIDER ================= */}
      <section id="home" className="hero-slider-wrap">
        <div className="slider-container">
          {sampleSliders.map((slide, index) => (
            <div key={slide.id} className={`slide-item ${index === activeSlide ? "active" : ""}`}>
              <div className="slide-text">
                <span className="slide-tag">{slide.tag}</span>
                <h2>{slide.title}</h2>
                <p>{slide.description}</p>
                <div className="slide-actions">
                  <a href="#courses" className="button button-accent">
                    {slide.cta} →
                  </a>
                  <a href="#subjects" className="button" style={{ background: "rgba(255,255,255,0.15)", color: "#fff" }}>
                    Tìm hiểu thêm
                  </a>
                </div>
              </div>
              <div className="slide-image-col">
                <img src={slide.image} alt={slide.title} className="slide-visual-img" />
              </div>
            </div>
          ))}

          {/* Slider controls */}
          <div className="slider-arrows">
            <button
              onClick={() => setActiveSlide((prev) => (prev === 0 ? sampleSliders.length - 1 : prev - 1))}
              className="slider-arrow-btn"
              title="Slide trước"
            >
              ←
            </button>
            <button
              onClick={() => setActiveSlide((prev) => (prev + 1) % sampleSliders.length)}
              className="slider-arrow-btn"
              title="Slide kế tiếp"
            >
              →
            </button>
          </div>

          <div className="slider-dots">
            {sampleSliders.map((_, i) => (
              <span
                key={i}
                onClick={() => setActiveSlide(i)}
                className={`slider-dot ${i === activeSlide ? "active" : ""}`}
              />
            ))}
          </div>
        </div>
      </section>

      {/* ================= 2. FEATURED SUBJECTS ================= */}
      <section id="subjects" className="featured-subjects-wrap">
        <div className="section-heading">
          <div>
            <div className="eyebrow">
              <span className="eyebrow-line"></span>
              <span>Chủ đề đào tạo tiêu biểu</span>
            </div>
            <h2>Khám phá theo lĩnh vực bạn quan tâm</h2>
          </div>
          <p className="section-aside">
            Các chuyên ngành được xây dựng bởi đội ngũ chuyên gia giàu kinh nghiệm thực chiến.
          </p>
        </div>

        <div className="subjects-grid">
          {sampleSubjects.map((sub) => (
            <div
              key={sub.id}
              className="subject-card"
              onClick={() => {
                setActiveCategory(sub.category);
                const el = document.getElementById("courses");
                if (el) el.scrollIntoView({ behavior: "smooth" });
              }}
            >
              <div>
                <div className="subject-card-icon">{sub.icon}</div>
                <h3>{sub.name}</h3>
                <p>{sub.desc}</p>
              </div>
              <div className="subject-card-meta">
                {sub.count} khóa học liên quan →
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ================= 3. COURSES CATALOG ================= */}
      <section id="courses" className="catalog">
        <div className="section-heading">
          <div>
            <div className="eyebrow">
              <span className="eyebrow-line"></span>
              <span>Chương trình học tập</span>
            </div>
            <h2>Danh sách Khóa học Active</h2>
          </div>
          <p className="section-aside">
            Đăng ký tham gia ngay với các gói học linh hoạt: từ gói 3 tháng, 6 tháng đến vĩnh viễn.
          </p>
        </div>

        {/* Search Bar */}
        <div className="search-bar">
          <label htmlFor="course-search">TÌM KIẾM KHÓA HỌC</label>
          <input
            id="course-search"
            type="text"
            placeholder="Nhập tên khóa học, giảng viên hoặc kỹ năng cần tìm..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setCurrentPage(1);
            }}
          />
          {searchQuery && (
            <button onClick={() => setSearchQuery("")} className="text-button" style={{ fontSize: "11px" }}>
              Xóa tìm kiếm
            </button>
          )}
        </div>

        {/* Category Toolbar */}
        <div className="catalog-toolbar">
          <div className="category-tabs">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => {
                  setActiveCategory(cat);
                  setCurrentPage(1);
                }}
                className={`category-tab ${activeCategory === cat ? "selected" : ""}`}
              >
                {cat}
              </button>
            ))}
          </div>
          <span className="course-count">Tìm thấy {filteredCourses.length} khóa học</span>
        </div>

        {/* Course Grid */}
        <div className="course-grid">
          {paginatedCourses.map((c) => (
            <div key={c.id} className="course-card">
              <div
                className={`course-art ${c.accent || "sage"}`}
                style={{
                  backgroundImage: c.image.startsWith("http")
                    ? `url(${c.image})`
                    : `url(https://images.unsplash.com/${c.image}?auto=format&fit=crop&w=700&q=80)`,
                }}
                onClick={() => setSelectedCourse(c)}
              >
                <span className="art-category">{c.category}</span>
                <span className="art-arrow">↗</span>
              </div>

              <div style={{ padding: "18px 0 0" }}>
                <div style={{ display: "flex", justifyContent: "space-between", fontSize: "11px", color: "var(--muted)", marginBottom: "6px" }}>
                  <span>{c.level} • {c.duration}</span>
                  <span style={{ color: "#d97706", fontWeight: 700 }}>★ {c.rating} ({c.students})</span>
                </div>

                <h3
                  onClick={() => setSelectedCourse(c)}
                  style={{ fontSize: "19px", fontFamily: "Georgia, serif", color: "var(--ink)", margin: "0 0 8px", cursor: "pointer" }}
                >
                  {c.title}
                </h3>
                <p style={{ fontSize: "13px", color: "var(--muted)", lineHeight: 1.5, marginBottom: "14px" }}>
                  {c.description}
                </p>

                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", borderTop: "1px solid var(--line)", paddingTop: "12px" }}>
                  <div>
                    <span style={{ fontSize: "10px", color: "var(--muted)", display: "block" }}>Giảng viên: {c.instructor}</span>
                    <strong style={{ fontSize: "17px", color: "var(--green)" }}>
                      Từ ${c.pricePackages?.[0]?.price ?? c.price}
                    </strong>
                  </div>

                  <div style={{ display: "flex", gap: "8px" }}>
                    <button
                      onClick={() => setSelectedCourse(c)}
                      className="button button-small"
                      style={{ background: "#eef2ec", color: "var(--ink)", border: "1px solid #dbe2d6" }}
                    >
                      Chi tiết
                    </button>
                    <button
                      onClick={() => {
                        setRegisterCourse(c);
                        setSelectedPackageId(c.pricePackages?.[0]?.id ?? null);
                      }}
                      className="button button-small button-accent"
                    >
                      Đăng ký
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Pagination Bar */}
        {totalPages > 1 && (
          <div className="pagination-bar">
            <button
              onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
              disabled={currentPage === 1}
              className="page-btn"
            >
              ← Trước
            </button>
            {Array.from({ length: totalPages }).map((_, i) => (
              <button
                key={i + 1}
                onClick={() => setCurrentPage(i + 1)}
                className={`page-btn ${currentPage === i + 1 ? "active" : ""}`}
              >
                {i + 1}
              </button>
            ))}
            <button
              onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
              disabled={currentPage === totalPages}
              className="page-btn"
            >
              Sau →
            </button>
          </div>
        )}
      </section>

      {/* ================= 4. BLOGS & POSTS SECTION ================= */}
      <section id="blogs" className="blogs-section">
        <div className="section-heading">
          <div>
            <div className="eyebrow">
              <span className="eyebrow-line"></span>
              <span>Góc chia sẻ & Kinh nghiệm</span>
            </div>
            <h2>Bài viết & Tin tức mới nhất</h2>
          </div>
          <p className="section-aside">
            Cập nhật xu hướng công nghệ, mẹo thiết kế và phương pháp học tập tối ưu từ ban chuyên môn.
          </p>
        </div>

        <div className="blogs-grid">
          {sampleBlogs.map((b) => (
            <div key={b.id} className="blog-card">
              <img src={b.image} alt={b.title} className="blog-card-img" />
              <div className="blog-card-body">
                <span className="blog-card-cat">{b.category}</span>
                <h3 className="blog-card-title">{b.title}</h3>
                <p className="blog-card-summary">{b.summary}</p>
                <div className="blog-card-footer">
                  <span>Tác giả: <strong>{b.author}</strong></span>
                  <span>{b.updatedAt}</span>
                </div>
                <button
                  onClick={() => setSelectedBlog(b)}
                  className="button button-small"
                  style={{ marginTop: "14px", background: "#f1f5ee", color: "var(--ink)", width: "100%" }}
                >
                  Đọc toàn bộ bài viết →
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ================= 5. FOOTER ================= */}
      <footer id="footer" style={{ background: "#1c2b23", color: "#b5c4ba", padding: "60px 6.1% 30px" }}>
        <div style={{ maxWidth: "1440px", margin: "auto", display: "grid", gridTemplateColumns: "1.5fr 1fr 1fr 1fr", gap: "40px", borderBottom: "1px solid rgba(255,255,255,0.1)", paddingBottom: "40px" }}>
          <div>
            <div style={{ fontFamily: "Georgia, serif", fontSize: "24px", color: "#fff", marginBottom: "12px" }}>
              OnlineLearn<span style={{ color: "var(--coral)" }}>.</span>
            </div>
            <p style={{ fontSize: "13px", lineHeight: "1.7", maxWidth: "320px" }}>
              Hệ thống đào tạo và khảo thí trực tuyến tiêu chuẩn cao dành cho học viên và chuyên gia xây dựng nội dung.
            </p>
          </div>
          <div>
            <h4 style={{ color: "#fff", fontSize: "13px", marginBottom: "14px" }}>DÀNH CHO HỌC VIÊN</h4>
            <ul style={{ listStyle: "none", padding: 0, margin: 0, fontSize: "12px", display: "grid", gap: "8px" }}>
              <li><a href="#courses">Danh sách khóa học</a></li>
              <li><Link href="/learn">Khóa học của tôi</Link></li>
              <li><Link href="/learn">Đơn đăng ký của tôi</Link></li>
              <li><a href="#home">Chính sách học phí</a></li>
            </ul>
          </div>
          <div>
            <h4 style={{ color: "#fff", fontSize: "13px", marginBottom: "14px" }}>QUẢN TRỊ & GIẢNG VIÊN</h4>
            <ul style={{ listStyle: "none", padding: 0, margin: 0, fontSize: "12px", display: "grid", gap: "8px" }}>
              <li><Link href="/manage">Cổng Quản trị Admin</Link></li>
              <li><Link href="/manage">Phân hệ Bán hàng (Sale)</Link></li>
              <li><Link href="/manage">Phân hệ Tiếp thị (Marketing)</Link></li>
              <li><Link href="/manage">Ngân hàng đề thi (Expert)</Link></li>
            </ul>
          </div>
          <div>
            <h4 style={{ color: "#fff", fontSize: "13px", marginBottom: "14px" }}>LIÊN HỆ HỖ TRỢ</h4>
            <p style={{ fontSize: "12px", lineHeight: "1.6" }}>
              Email: support@onlinelearn.edu.vn<br />
              Hotline: 1900 6868 (8:00 - 21:00)<br />
              Khu Công nghệ cao, Hà Nội
            </p>
          </div>
        </div>
        <div style={{ maxWidth: "1440px", margin: "auto", paddingTop: "20px", display: "flex", justifyContent: "space-between", fontSize: "11px", opacity: 0.7 }}>
          <span>© 2026 OnlineLearn Platform. All rights reserved.</span>
          <span>Bản quyền thiết kế theo đặc tả chuẩn OnlineLearn</span>
        </div>
      </footer>

      {/* ================= MODAL: COURSE DETAILS ================= */}
      {selectedCourse && (
        <div className="dialog-overlay" onClick={() => setSelectedCourse(null)}>
          <div className="dialog course-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "680px" }}>
            <button className="dialog-close" onClick={() => setSelectedCourse(null)}>✕</button>

            <div style={{ display: "flex", gap: "10px", alignItems: "center", marginBottom: "10px" }}>
              <span className="art-category" style={{ position: "static" }}>{selectedCourse.category}</span>
              <span style={{ fontSize: "12px", color: "var(--muted)" }}>{selectedCourse.level} • {selectedCourse.duration}</span>
            </div>

            <h2>{selectedCourse.title}</h2>
            <p style={{ color: "var(--muted)", fontSize: "14px", lineHeight: "1.7", marginBottom: "20px" }}>
              {selectedCourse.description}
            </p>

            <div style={{ background: "#f8faf6", border: "1px solid #dfe5dc", borderRadius: "8px", padding: "16px", marginBottom: "24px" }}>
              <h4 style={{ margin: "0 0 10px", fontSize: "13px" }}>Giảng viên hướng dẫn:</h4>
              <p style={{ margin: 0, fontSize: "14px", fontWeight: 700, color: "var(--green)" }}>{selectedCourse.instructor}</p>
            </div>

            <h3 style={{ fontSize: "16px", fontFamily: "Georgia, serif", marginBottom: "12px" }}>Các gói học phí có sẵn:</h3>
            <div style={{ display: "grid", gap: "10px", marginBottom: "24px" }}>
              {(selectedCourse.pricePackages || [{ id: 1, name: "Gói tiêu chuẩn", price: selectedCourse.price, currency: "USD", accessDays: 0, published: true }]).map((pkg) => (
                <div
                  key={pkg.id}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    padding: "12px 16px",
                    border: "1px solid #dfe5dc",
                    borderRadius: "6px",
                    background: "#fff",
                  }}
                >
                  <div>
                    <strong style={{ fontSize: "14px" }}>{pkg.name}</strong>
                    <span style={{ display: "block", fontSize: "11px", color: "var(--muted)" }}>
                      {pkg.accessDays === 0 ? "Hạn truy cập: Vĩnh viễn" : `Hạn truy cập: ${pkg.accessDays} ngày`}
                    </span>
                  </div>
                  <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                    <span style={{ fontSize: "16px", fontWeight: 700, color: "var(--green)" }}>
                      ${pkg.price} {pkg.currency}
                    </span>
                    <button
                      onClick={() => {
                        setRegisterCourse(selectedCourse);
                        setSelectedPackageId(pkg.id);
                        setSelectedCourse(null);
                      }}
                      className="button button-small button-accent"
                    >
                      Chọn gói này
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div style={{ display: "flex", justifyContent: "flex-end" }}>
              <button onClick={() => setSelectedCourse(null)} className="text-button">
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ================= MODAL: COURSE REGISTER (GUEST REGISTRATION) ================= */}
      {registerCourse && (
        <div className="dialog-overlay" onClick={() => setRegisterCourse(null)}>
          <div className="dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "520px" }}>
            <button className="dialog-close" onClick={() => setRegisterCourse(null)}>✕</button>

            <span className="eyebrow" style={{ marginBottom: "8px" }}>ĐĂNG KÝ KHÓA HỌC</span>
            <h2 style={{ fontSize: "24px", marginBottom: "8px" }}>{registerCourse.title}</h2>
            <p style={{ fontSize: "13px", color: "var(--muted)", marginBottom: "20px" }}>
              Khách có thể đăng ký trực tiếp. Sau khi gửi, đơn sẽ được ghi nhận và nhân viên tư vấn sẽ liên hệ kích hoạt.
            </p>

            <form onSubmit={submitRegistration} style={{ display: "grid", gap: "14px" }}>
              <div>
                <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                  HỌ VÀ TÊN *
                </label>
                <input
                  type="text"
                  name="fullName"
                  defaultValue={user?.fullName || ""}
                  required
                  placeholder="Nguyễn Văn A"
                  style={{ width: "100%", padding: "10px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                />
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px" }}>
                <div>
                  <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                    GIỚI TÍNH *
                  </label>
                  <select
                    name="gender"
                    style={{ width: "100%", padding: "10px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                  >
                    <option value="Male">Nam</option>
                    <option value="Female">Nữ</option>
                    <option value="Other">Khác</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                    SỐ ĐIỆN THOẠI *
                  </label>
                  <input
                    type="tel"
                    name="phone"
                    required
                    placeholder="0912 345 678"
                    style={{ width: "100%", padding: "10px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                  ĐỊA CHỈ EMAIL LIÊN HỆ *
                </label>
                <input
                  type="email"
                  name="email"
                  defaultValue={user?.email || ""}
                  required
                  placeholder="email@example.com"
                  style={{ width: "100%", padding: "10px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                />
              </div>

              <div>
                <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                  CHỌN GÓI HỌC PHÍ *
                </label>
                <select
                  value={selectedPackageId ?? ""}
                  onChange={(e) => setSelectedPackageId(Number(e.target.value))}
                  required
                  style={{ width: "100%", padding: "10px", border: "1px solid #d8dfd4", borderRadius: "4px" }}
                >
                  {(registerCourse.pricePackages || [{ id: 1, name: "Gói tiêu chuẩn", price: registerCourse.price, currency: "USD" }]).map((pkg) => (
                    <option key={pkg.id} value={pkg.id}>
                      {pkg.name} - ${pkg.price} {pkg.currency}
                    </option>
                  ))}
                </select>
              </div>

              <div style={{ marginTop: "10px", display: "flex", gap: "10px", justifyContent: "flex-end" }}>
                <button type="button" onClick={() => setRegisterCourse(null)} className="text-button">
                  Hủy bỏ
                </button>
                <button type="submit" disabled={busy} className="button button-accent">
                  {busy ? "Đang gửi đơn..." : "Gửi Đơn Đăng Ký"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ================= MODAL: BLOG DETAIL ================= */}
      {selectedBlog && (
        <div className="dialog-overlay" onClick={() => setSelectedBlog(null)}>
          <div className="dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "760px", maxHeight: "90vh", overflowY: "auto" }}>
            <button className="dialog-close" onClick={() => setSelectedBlog(null)}>✕</button>

            <img
              src={selectedBlog.image}
              alt={selectedBlog.title}
              style={{ width: "100%", height: "260px", objectFit: "cover", borderRadius: "8px", marginBottom: "20px" }}
            />

            <span className="blog-card-cat">{selectedBlog.category}</span>
            <h2 style={{ fontSize: "28px", margin: "8px 0 12px" }}>{selectedBlog.title}</h2>

            <div style={{ display: "flex", gap: "20px", fontSize: "12px", color: "var(--muted)", borderBottom: "1px solid var(--line)", paddingBottom: "14px", marginBottom: "20px" }}>
              <span>✍️ Tác giả: <strong>{selectedBlog.author}</strong></span>
              <span>📅 Ngày cập nhật: {selectedBlog.updatedAt}</span>
            </div>

            <div style={{ fontSize: "15px", lineHeight: "1.8", color: "var(--ink)" }}>
              <p style={{ fontSize: "16px", fontWeight: 700, color: "var(--green)" }}>{selectedBlog.summary}</p>
              <p>{selectedBlog.content}</p>
              <p>Morrow Online Learning cam kết cung cấp kiến thức nền tảng và phương pháp thực chiến, giúp người học làm chủ kỹ năng một cách vững chắc nhất.</p>
            </div>

            <div style={{ marginTop: "30px", display: "flex", justifyContent: "flex-end" }}>
              <button onClick={() => setSelectedBlog(null)} className="button button-small button-dark">
                Đóng bài viết
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ================= MODAL: AUTH (LOGIN, REGISTER, RESET PASSWORD) ================= */}
      {authMode && (
        <div className="dialog-overlay" onClick={() => setAuthMode(null)}>
          <div className="dialog auth-dialog" onClick={(e) => e.stopPropagation()} style={{ maxWidth: "480px" }}>
            <button className="dialog-close" onClick={() => setAuthMode(null)}>✕</button>

            <h2 style={{ fontSize: "26px", marginBottom: "6px" }}>
              {authMode === "login" && "Đăng nhập Tài khoản"}
              {authMode === "register" && "Đăng ký Thành viên"}
              {authMode === "forgot" && "Quên Mật khẩu"}
            </h2>
            <p style={{ fontSize: "12px", color: "var(--muted)", marginBottom: "20px" }}>
              {authMode === "login" && "Đăng nhập để vào không gian học tập hoặc trang quản lý của bạn."}
              {authMode === "register" && "Tạo tài khoản mới để lưu tiến độ học tập và đơn đăng ký."}
              {authMode === "forgot" && "Nhập email của bạn để nhận mã liên kết đặt lại mật khẩu mới."}
            </p>

            {/* Quick Demo Login Preset Buttons for easy testing */}
            {authMode === "login" && (
              <div style={{ background: "#f0f4ec", padding: "12px", borderRadius: "8px", marginBottom: "18px" }}>
                <span style={{ fontSize: "10px", fontWeight: 700, color: "var(--green)", display: "block", marginBottom: "8px" }}>
                  ⚡ TEST NHANH VỚI CÁC ACTOR (BẤM ĐỂ CHUYỂN ROLE):
                </span>
                <div style={{ display: "flex", flexWrap: "wrap", gap: "6px" }}>
                  <button type="button" onClick={() => demoLogin("admin@example.com", "ADMIN")} className="actor-btn" style={{ background: "#315a45" }}>
                    👑 Admin
                  </button>
                  <button type="button" onClick={() => demoLogin("customer@example.com", "CUSTOMER")} className="actor-btn" style={{ background: "#2e7d32" }}>
                    🎓 Customer
                  </button>
                  <button type="button" onClick={() => demoLogin("sale@example.com", "SALE")} className="actor-btn" style={{ background: "#c25528" }}>
                    💼 Sale
                  </button>
                  <button type="button" onClick={() => demoLogin("marketing@example.com", "MARKETING")} className="actor-btn" style={{ background: "#7c3aed" }}>
                    📢 Marketing
                  </button>
                  <button type="button" onClick={() => demoLogin("expert@example.com", "EXPERT")} className="actor-btn" style={{ background: "#0284c7" }}>
                    ✍️ Expert
                  </button>
                </div>
              </div>
            )}

            <form onSubmit={submitAuth} style={{ display: "grid", gap: "12px" }}>
              {authMode === "register" && (
                <>
                  <div>
                    <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                      HỌ VÀ TÊN
                    </label>
                    <input type="text" name="fullName" required placeholder="Nguyễn Văn A" style={{ width: "100%", padding: "9px", border: "1px solid #d8dfd4", borderRadius: "4px" }} />
                  </div>
                  <div>
                    <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                      SỐ ĐIỆN THOẠI
                    </label>
                    <input type="tel" name="phone" placeholder="0987 654 321" style={{ width: "100%", padding: "9px", border: "1px solid #d8dfd4", borderRadius: "4px" }} />
                  </div>
                </>
              )}

              <div>
                <label style={{ display: "block", fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                  ĐỊA CHỈ EMAIL
                </label>
                <input type="email" name="email" required placeholder="admin@example.com" style={{ width: "100%", padding: "9px", border: "1px solid #d8dfd4", borderRadius: "4px" }} />
              </div>

              {authMode !== "forgot" && (
                <div>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <label style={{ fontSize: "11px", fontWeight: 700, marginBottom: "4px" }}>
                      MẬT KHẨU
                    </label>
                    {authMode === "login" && (
                      <button type="button" onClick={() => setAuthMode("forgot")} className="text-button" style={{ fontSize: "11px", color: "var(--coral)" }}>
                        Quên mật khẩu?
                      </button>
                    )}
                  </div>
                  <input type="password" name="password" required placeholder="••••••••" style={{ width: "100%", padding: "9px", border: "1px solid #d8dfd4", borderRadius: "4px" }} />
                </div>
              )}

              <button type="submit" disabled={busy} className="button button-dark" style={{ marginTop: "8px" }}>
                {busy ? "Đang xử lý..." : authMode === "login" ? "Đăng nhập" : authMode === "register" ? "Đăng ký tài khoản" : "Gửi link reset mật khẩu"}
              </button>

              <div style={{ textAlign: "center", marginTop: "12px", fontSize: "12px" }}>
                {authMode === "login" && (
                  <span>
                    Chưa có tài khoản?{" "}
                    <button type="button" onClick={() => setAuthMode("register")} className="text-button" style={{ color: "var(--green)" }}>
                      Đăng ký ngay
                    </button>
                  </span>
                )}
                {authMode === "register" && (
                  <span>
                    Đã có tài khoản?{" "}
                    <button type="button" onClick={() => setAuthMode("login")} className="text-button" style={{ color: "var(--green)" }}>
                      Đăng nhập
                    </button>
                  </span>
                )}
                {authMode === "forgot" && (
                  <button type="button" onClick={() => setAuthMode("login")} className="text-button" style={{ color: "var(--green)" }}>
                    ← Quay lại Đăng nhập
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
