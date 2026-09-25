import urllib.request
import base64
import json
import os
import shutil

mermaid_code = """classDiagram
    direction TB

    class BaseEntity {
        <<abstract>>
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
    }

    class Role {
        +Long id
        +String code
        +String name
        +String description
    }

    class User {
        +Long id
        +String fullName
        +Gender gender
        +String email
        +String mobile
        +String password
        +String avatar
        +String address
        +UserStatus status
        +Boolean emailVerified
        +String verificationToken
        +LocalDateTime verificationTokenExpiry
        +String resetPasswordToken
        +LocalDateTime resetPasswordTokenExpiry
    }

    class SubjectCategory {
        +Long id
        +String name
        +String description
        +Boolean status
    }

    class DimensionType {
        +Long id
        +String name
        +String description
        +Boolean status
    }

    class SubjectDimension {
        +Long id
        +String name
        +String description
    }

    class Subject {
        +Long id
        +String name
        +String thumbnail
        +String briefInfo
        +String description
        +Boolean featured
        +SubjectStatus status
    }

    class Lesson {
        +Long id
        +String name
        +Integer orderNum
        +LessonTypeEnum type
        +String videoLink
        +String htmlContent
        +LessonStatus status
    }

    class QuestionLevel {
        +Long id
        +String name
        +String code
        +String description
        +Boolean status
    }

    class TestType {
        +Long id
        +String name
        +String code
        +String description
        +Boolean status
    }

    class Question {
        +Long id
        +QuestionStatus status
        +String content
        +String mediaUrl
        +MediaType mediaType
        +String explanation
    }

    class AnswerOption {
        +Long id
        +String content
        +Boolean isCorrect
    }

    class Quiz {
        +Long id
        +String name
        +Integer duration
        +Double passRate
        +String description
    }

    class QuizQuestion {
        +Long id
        +Integer orderNum
    }

    class QuizAttempt {
        +Long id
        +LocalDateTime startTime
        +LocalDateTime finishTime
        +QuizAttemptStatus status
        +Double score
        +Integer currentQuestionIndex
        +Boolean isPassed
    }

    class QuizAnswer {
        +Long id
        +Boolean markedForReview
        +Boolean isCorrect
    }

    class PricePackage {
        +Long id
        +String packageName
        +Integer accessDuration
        +BigDecimal listPrice
        +BigDecimal salePrice
        +String description
        +PackageStatus status
    }

    class Registration {
        +Long id
        +String fullName
        +Gender gender
        +String email
        +String mobile
        +LocalDateTime registrationTime
        +RegistrationStatus status
        +BigDecimal totalCost
        +LocalDateTime validFrom
        +LocalDateTime validTo
        +String notes
    }

    class CourseAccess {
        +Long id
        +LocalDateTime grantedDate
        +LocalDateTime expiryDate
        +CourseAccessStatus status
    }

    class PostCategory {
        +Long id
        +String name
        +String description
        +Boolean status
    }

    class Post {
        +Long id
        +String title
        +String briefInfo
        +String thumbnail
        +String content
        +PostStatus status
    }

    class Slider {
        +Long id
        +String name
        +String image
        +String link
        +Integer orderNum
        +SliderStatus status
        +String notes
    }

    class Setting {
        +Long id
        +SettingType type
        +String code
        +String value
        +String description
        +Integer orderNum
        +Boolean status
    }

    BaseEntity <|-- User
    BaseEntity <|-- SubjectCategory
    BaseEntity <|-- DimensionType
    BaseEntity <|-- SubjectDimension
    BaseEntity <|-- Subject
    BaseEntity <|-- Lesson
    BaseEntity <|-- QuestionLevel
    BaseEntity <|-- TestType
    BaseEntity <|-- Question
    BaseEntity <|-- AnswerOption
    BaseEntity <|-- Quiz
    BaseEntity <|-- QuizQuestion
    BaseEntity <|-- QuizAttempt
    BaseEntity <|-- QuizAnswer
    BaseEntity <|-- PricePackage
    BaseEntity <|-- Registration
    BaseEntity <|-- CourseAccess
    BaseEntity <|-- PostCategory
    BaseEntity <|-- Post
    BaseEntity <|-- Slider
    BaseEntity <|-- Setting

    User --> Role : role
    Subject --> User : owner
    Subject --> SubjectCategory : category
    Subject "1" *-- "*" PricePackage : pricePackages
    Subject "1" *-- "*" SubjectDimension : dimensions
    Subject "1" *-- "*" Lesson : lessons
    SubjectDimension --> DimensionType : type

    Lesson --> Lesson : parentLesson
    Lesson --> Quiz : quiz

    Quiz --> Subject : subject
    Quiz --> QuestionLevel : level
    Quiz --> TestType : quizType
    Quiz "1" *-- "*" QuizQuestion : quizQuestions
    QuizQuestion --> Question : question
    Quiz "1" *-- "*" QuizAttempt : attempts

    Question --> Subject : subject
    Question --> Lesson : lesson
    Question --> QuestionLevel : level
    Question "1" *-- "*" AnswerOption : answers
    Question "*" -- "*" SubjectDimension : dimensions

    QuizAttempt --> User : customer
    QuizAttempt "1" *-- "*" QuizAnswer : answers
    QuizAnswer --> Question : question
    QuizAnswer --> AnswerOption : selectedOption

    Registration --> Subject : subject
    Registration --> PricePackage : pricePackage
    Registration --> User : sale

    CourseAccess --> User : customer
    CourseAccess --> Subject : subject
    CourseAccess --> Registration : registration

    Post --> User : author
    Post --> PostCategory : category
"""

state = json.dumps({"code": mermaid_code, "mermaid": {"theme": "default"}})
b64 = base64.urlsafe_b64encode(state.encode("utf-8")).decode("ascii")

os.makedirs("docs", exist_ok=True)

# Fetch SVG
print("Downloading SVG...")
req_svg = urllib.request.Request(f"https://mermaid.ink/svg/{b64}", headers={"User-Agent": "Mozilla/5.0"})
with urllib.request.urlopen(req_svg) as resp:
    svg_data = resp.read()
    with open("docs/class_diagram.svg", "wb") as f:
        f.write(svg_data)
    print(f"SVG saved: {len(svg_data)} bytes")

# Fetch PNG
print("Downloading PNG...")
req_png = urllib.request.Request(f"https://mermaid.ink/img/{b64}", headers={"User-Agent": "Mozilla/5.0"})
with urllib.request.urlopen(req_png) as resp:
    png_data = resp.read()
    with open("docs/class_diagram.png", "wb") as f:
        f.write(png_data)
    print(f"PNG saved: {len(png_data)} bytes")

# Also copy to artifact directory
artifact_dir = r"C:\Users\Admin\.gemini\antigravity-ide\brain\62297dab-8154-4d01-b3a5-9fde998daf41"
if os.path.exists(artifact_dir):
    shutil.copy("docs/class_diagram.png", os.path.join(artifact_dir, "class_diagram.png"))
    shutil.copy("docs/class_diagram.svg", os.path.join(artifact_dir, "class_diagram.svg"))
    print("Copied images to artifact directory.")
