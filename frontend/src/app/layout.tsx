import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "OnlineLearn | Nền Tảng Học Trực Tuyến & Khảo Thí Toàn Diện",
  description: "Khám phá khóa học chất lượng cao, học tập tương tác, thi trắc nghiệm và quản trị đào tạo.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="vi">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:ital,wght@0,300..800;1,300..800&display=swap"
          rel="stylesheet"
        />
      </head>
      <body>{children}</body>
    </html>
  );
}
