package hello.livsi_0820.controller;

import hello.livsi_0820.entity.Member;
import hello.livsi_0820.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Member API", description = "회원 정보 관리를 위한 API")
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }


    @GetMapping
    @Operation(summary = "모든 회원 조회", description = "시스템에 등록된 모든 회원 목록을 조회합니다.")
    public List<Member> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 회원 조회", description = "ID를 사용하여 특정 회원을 조회합니다.")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return memberService.getMemberById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "신규 회원 생성", description = "새로운 회원을 시스템에 등록합니다.")
    public Member createMember(@RequestBody Member member) {
        return memberService.createMember(member);
    }

    @PutMapping("/{id}")
    @Operation(summary = "회원 정보 수정", description = "ID를 사용하여 기존 회원의 정보를 수정합니다.")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member memberDetails) {
        try {
            Member updated = memberService.updateMember(id, memberDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "회원 삭제", description = "ID를 사용하여 특정 회원을 삭제합니다.")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
