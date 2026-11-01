package com.example.GYM_management_api.services;
 
import com.example.GYM_management_api.dtos.MembershipDto;
 
import java.util.List;
 
public interface IMembershipService {
 
    List<MembershipDto> getAllMemberships(String status);
 
    MembershipDto getMembershipById(Long id);
 
    MembershipDto createMembership(MembershipDto dto);
 
    MembershipDto updateMembership(Long id, MembershipDto dto);
 
    void deleteMembership(Long id);
}
