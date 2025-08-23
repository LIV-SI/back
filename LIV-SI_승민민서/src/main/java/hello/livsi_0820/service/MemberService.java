package hello.livsi_0820.service;

import hello.livsi_0820.entity.Member;
import hello.livsi_0820.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    public Member updateMember(Long memberId, Member memberDetails) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setMemberName(memberDetails.getMemberName());
        member.setPassword(memberDetails.getPassword());
        member.setEmail(memberDetails.getEmail());
        return memberRepository.save(member);
    }

    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }
}
