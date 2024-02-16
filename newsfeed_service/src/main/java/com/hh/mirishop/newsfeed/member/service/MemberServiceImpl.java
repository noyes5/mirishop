package com.hh.newsfeed.member.service;

import com.hh.newsfeed.auth.domain.UserDetailsImpl;
import com.hh.newsfeed.common.exception.ErrorCode;
import com.hh.newsfeed.common.exception.MemberException;
import com.hh.newsfeed.member.domain.Role;
import com.hh.newsfeed.member.dto.ChangePasswordRequest;
import com.hh.newsfeed.member.dto.MemberRequest;
import com.hh.newsfeed.member.dto.MemberUpdateRequest;
import com.hh.newsfeed.member.entity.Member;
import com.hh.newsfeed.member.repository.MemberRepository;
import com.hh.newsfeed.common.constants.UserConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    // 기본 이미지 경로는 추후 업로드 방식이 변경되면 수정 필요
    private static final String DEFAULT_PROFILE_IMAGE_PATH = "/uploads/images/default.jpg";
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public Long register(final MemberRequest memberRequest) {
        String email = memberRequest.getEmail();
        String password = memberRequest.getPassword();
        String profileImagePath = memberRequest.getProfileImage();

        validateEmail(email);
        validatePassword(password);
        validateUploadProfileImage(profileImagePath);

        String encodedPassword = encodePassword(password);

        final Member user = Member.builder()
                .nickname(memberRequest.getName())
                .email(memberRequest.getEmail())
                .password(encodedPassword)
                .profileImage(memberRequest.getProfileImage())
                .bio(memberRequest.getBio())
                .role(Role.ROLE_USER) // role 설정
                .isDeleted(false) // 기본값 false
                .build();

        final Member userEntity = memberRepository.save(user);

        return userEntity.getNumber();
    }

    @Override
    @Transactional
    public void update(MemberUpdateRequest memberUpdateRequest, UserDetailsImpl userDetails) {
        Member member = memberRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        String nickname = memberUpdateRequest.getNickName();
        String profileImagePath = memberUpdateRequest.getProfileImage();
        String bio = memberUpdateRequest.getBio();

        validateUploadProfileImage(profileImagePath);

        member.updateNickname(nickname);
        member.updateProfileImage(profileImagePath);
        member.updateBio(bio);

        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest, UserDetailsImpl userDetails) {
        Member member = memberRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
        String storedPassword = member.getPassword();
        String oldPassword = changePasswordRequest.getOldPassword();
        String newPassword = changePasswordRequest.getNewPassword();
        validatePassword(newPassword);

        // 기존 비밀번호 검증 로직
        if (!isMatchesPassword(oldPassword, storedPassword)) {
            throw new MemberException(ErrorCode.WRONG_PASSWORD);
        }

        // 새로운 비밀번호가 기존 비밀번호와 같은 경우
        if (isMatchesPassword(newPassword, storedPassword)) {
            throw new MemberException(ErrorCode.INVALID_PASSWORD);
        }

        member.updatePassword(encodePassword(newPassword));
        memberRepository.save(member);
    }

    private void validateEmail(final String email) {
        validatedEmailForm(email);
        validatedDuplicatedEmail(email);
    }

    private void validatedEmailForm(final String email) {
        if (!UserConstants.EMAIL_REGEX.matcher(email).matches()) {
            throw new MemberException(ErrorCode.INVALID_EMAIL_FROM);
        }
    }

    private void validatedDuplicatedEmail(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(existingUser -> {
                    throw new MemberException(ErrorCode.DUPLICATED_EMAIL);
                });
    }

    private void validatePassword(String password) {
        if (password.length() < UserConstants.USER_PASSWORD_LENGTH) {
            throw new MemberException(ErrorCode.INVALID_PASSWORD_LENGTH);
        }
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private void validateUploadProfileImage(String profileImagePath) {
        if (profileImagePath == null || profileImagePath.isEmpty()) {
            profileImagePath = DEFAULT_PROFILE_IMAGE_PATH;
        }
    }

    private boolean isMatchesPassword(String password, String storedPassword) {
        return bCryptPasswordEncoder.matches(password, storedPassword);
    }
}