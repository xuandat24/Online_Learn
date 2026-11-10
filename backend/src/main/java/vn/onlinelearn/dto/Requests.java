package vn.onlinelearn.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public final class Requests {
    private Requests() {}
    public record SignUp(@NotBlank @Size(max=150) String fullName, @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=8,max=72) String password) {}
    public record Login(@NotBlank @Email String email, @NotBlank String password) {}
    public record EmailRequest(@NotBlank @Email String email) {}
    public record Reset(@NotBlank String token, @NotBlank @Size(min=8,max=72) String password, @NotBlank String confirmPassword) {}
    public record PasswordChange(@NotBlank String currentPassword, @NotBlank @Size(min=8,max=72) String password, @NotBlank String confirmPassword) {}
    public record Profile(@NotBlank @Size(max=150) String fullName, @Size(max=20) String gender, @Size(max=20) String mobile, @Size(max=255) String address, @Size(max=500) String avatarUrl) {}
    public record Course(@NotBlank @Size(max=200) String name, @NotNull Long ownerUserId, @NotNull Long categorySettingId,
        @Size(max=255) String tagline, @Size(max=500) String thumbnailUrl, @Size(max=100000) String description, boolean featured,
        @NotNull @Pattern(regexp="Published|Unpublished") String publishStatus) {}
    public record PackageInput(@NotBlank @Size(max=150) String name, @Min(1) @Max(120) int durationMonths,
        @NotNull @DecimalMin("1000") @DecimalMax("1000000000") BigDecimal listPrice,
        @NotNull @DecimalMin("1000") @DecimalMax("1000000000") BigDecimal salePrice,
        @Size(max=500) String description, @NotNull @Pattern(regexp="Active|Inactive") String status) {}
    public record LessonInput(@NotBlank @Size(max=200) String name, @NotNull @Pattern(regexp="Subject Topic|Lesson|Quiz") String lessonType,
        @Min(0) int sortOrder, Long packageId, Long parentTopicId, Long quizId, @Size(max=500) String videoUrl,
        @Size(max=200000) String htmlContent, @NotNull @Pattern(regexp="Active|Inactive") String status) {}
    public record DimensionInput(@NotNull Long typeSettingId, @NotBlank @Size(max=150) String name, @Size(max=500) String description) {}
    public record RegistrationInput(@NotNull Long packageId, @NotBlank @Size(max=150) String fullName,
        @NotBlank @Email @Size(max=254) String email, @NotBlank @Pattern(regexp="[+0-9 ()-]{8,20}") String mobile, @Size(max=20) String gender) {}
    public record RegistrationEdit(@NotBlank @Size(max=150) String fullName, @NotBlank @Pattern(regexp="[+0-9 ()-]{8,20}") String mobile, @Size(max=20) String gender, @Size(max=2000) String notes) {}
    public record OptionInput(@NotBlank @Size(max=4000) String content, boolean correct) {}
    public record QuestionInput(@NotBlank @Size(max=10000) String content, @NotNull Long levelSettingId, Long lessonId,
        @Size(max=500) String mediaUrl, @Pattern(regexp="image|video|audio") String mediaType, @Size(max=10000) String explanation,
        @NotNull @Pattern(regexp="Active|Inactive") String status, @NotNull @Size(min=2,max=8) List<@Valid OptionInput> options, Set<Long> dimensionIds) {}
    public record QuizInput(@NotBlank @Size(max=200) String name, @NotNull Long quizTypeSettingId, Long levelSettingId,
        @Min(1) @Max(180) int durationMinutes, @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal passRate,
        @Size(max=10000) String description, @NotNull @Size(min=1,max=100) List<Long> questionIds) {}
    public record AnswerInput(@NotNull Long questionId, Long optionId, boolean markedForReview) {}
    public record QuestionImport(@NotNull @Size(min=1,max=100) List<@Valid QuestionInput> questions) {}
    public record PostInput(@NotBlank @Size(max=250) String title, @NotNull Long categorySettingId,
        @Size(max=500) String thumbnailUrl, @Size(max=500) String briefInfo, @NotBlank @Size(max=200000) String body,
        boolean featured, @NotNull @Pattern(regexp="Visible|Hidden") String status) {}
    public record SliderInput(@NotBlank @Size(max=250) String title, @NotBlank @Size(max=500) String imageUrl,
        @Size(max=500) String backlink, @NotNull @Pattern(regexp="Visible|Hidden") String status, @Size(max=500) String notes) {}
    public record UserCreate(@NotBlank @Size(max=150) String fullName, @NotBlank @Email @Size(max=254) String email, @NotBlank String roleCode) {}
    public record UserUpdate(@NotBlank String roleCode, @NotNull @Pattern(regexp="Active|Inactive") String status) {}
    public record SettingInput(@NotBlank @Size(max=50) String type, @NotBlank @Size(max=200) String value, @Min(0) int sortOrder,
        @Size(max=500) String description, @NotNull @Pattern(regexp="Active|Inactive") String status) {}
}
