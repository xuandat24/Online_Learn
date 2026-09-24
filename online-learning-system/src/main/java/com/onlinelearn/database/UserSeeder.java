package com.onlinelearn.database;

import com.onlinelearn.entity.Role;
import com.onlinelearn.entity.User;
import com.onlinelearn.entity.enums.Gender;
import com.onlinelearn.entity.enums.UserStatus;
import com.onlinelearn.repository.RoleRepository;
import com.onlinelearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seedUsers() {
        log.info("--> Seeding Users...");

        Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not found"));
        Role expertRole = roleRepository.findByCode("EXPERT")
                .orElseThrow(() -> new IllegalStateException("EXPERT role not found"));
        Role saleRole = roleRepository.findByCode("SALE")
                .orElseThrow(() -> new IllegalStateException("SALE role not found"));
        Role marketingRole = roleRepository.findByCode("MARKETING")
                .orElseThrow(() -> new IllegalStateException("MARKETING role not found"));

        if (!userRepository.existsByEmail("admin@onlinelearn.com")) {
            userRepository.save(User.builder()
                    .fullName("Admin System")
                    .email("admin@onlinelearn.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .mobile("0901234567")
                    .gender(Gender.MALE)
                    .address("Hà Nội, Việt Nam")
                    .role(adminRole)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .avatar("https://api.dicebear.com/7.x/bottts/svg?seed=AdminSystem")
                    .build());
        }

        if (!userRepository.existsByEmail("customer@onlinelearn.com")) {
            userRepository.save(User.builder()
                    .fullName("Nguyễn Văn A")
                    .email("customer@onlinelearn.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .mobile("0987654321")
                    .gender(Gender.MALE)
                    .address("Cầu Giấy, Hà Nội")
                    .role(customerRole)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=NguyenVanA")
                    .build());
        }

        if (!userRepository.existsByEmail("expert@onlinelearn.com")) {
            userRepository.save(User.builder()
                    .fullName("Trần Văn B")
                    .email("expert@onlinelearn.com")
                    .password(passwordEncoder.encode("Expert@123"))
                    .mobile("0912345678")
                    .gender(Gender.MALE)
                    .address("Đống Đa, Hà Nội")
                    .role(expertRole)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=TranVanB")
                    .build());
        }

        if (!userRepository.existsByEmail("sale@onlinelearn.com")) {
            userRepository.save(User.builder()
                    .fullName("Lê Thị C")
                    .email("sale@onlinelearn.com")
                    .password(passwordEncoder.encode("Sale@123"))
                    .mobile("0934567890")
                    .gender(Gender.FEMALE)
                    .address("Thanh Xuân, Hà Nội")
                    .role(saleRole)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=LeThiC")
                    .build());
        }

        if (!userRepository.existsByEmail("marketing@onlinelearn.com")) {
            userRepository.save(User.builder()
                    .fullName("Phạm Văn D")
                    .email("marketing@onlinelearn.com")
                    .password(passwordEncoder.encode("Marketing@123"))
                    .mobile("0945678901")
                    .gender(Gender.MALE)
                    .address("Ba Đình, Hà Nội")
                    .role(marketingRole)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .avatar("https://api.dicebear.com/7.x/avataaars/svg?seed=PhamVanD")
                    .build());
        }

        log.info("✓ Users seeded successfully.");
    }
}
