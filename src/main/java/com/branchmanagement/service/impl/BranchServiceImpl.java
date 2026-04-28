package com.branchmanagement.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.branchmanagement.dto.BranchResponse;
import com.branchmanagement.entity.Branch;
import com.branchmanagement.repository.BranchRepository;
import com.branchmanagement.service.BranchService;

@Service
@Transactional(readOnly = true)
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository;

	public BranchServiceImpl(BranchRepository branchRepository) {
		this.branchRepository = branchRepository;
	}

	@Override
	public List<BranchResponse> getAllBranches() {
		return branchRepository.findAllByOrderByBranchNameAsc().stream().map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public List<BranchResponse> getActiveBranches() {
		return branchRepository.findByStatus("active").stream().map(this::toResponse).collect(Collectors.toList());
	}

	@Override
	public BranchResponse getBranchById(Integer id) {
		Branch branch = branchRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh với ID: " + id));
		return toResponse(branch);
	}

	@Override
	public BranchResponse getBranchByCode(String branchCode) {
		Branch branch = branchRepository.findByBranchCode(branchCode)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh với mã: " + branchCode));
		return toResponse(branch);
	}

	@Override
	@Transactional
	public Branch createBranch(Branch request) {

		if (branchRepository.existsByBranchCode(request.getBranchCode())) {
			throw new RuntimeException("Branch code đã tồn tại");
		}

		request.setStatus("active");

		return branchRepository.save(request);
	}

	@Override
	@Transactional
	public Branch updateBranch(Integer id, Branch request) {

		Branch branch = branchRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));

		branch.setBranchName(request.getBranchName());
		branch.setAddress(request.getAddress());
		branch.setLatitude(request.getLatitude());
		branch.setLongitude(request.getLongitude());

		return branchRepository.save(branch);
	}

	@Override
	@Transactional
	public void toggleStatus(Integer id) {

		Branch branch = branchRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh"));

		if ("active".equalsIgnoreCase(branch.getStatus())) {
			branch.setStatus("inactive");
		} else {
			branch.setStatus("active");
		}

		branchRepository.save(branch);
	}

	private BranchResponse toResponse(Branch branch) {

		Double lat = branch.getLatitude() != null ? branch.getLatitude().doubleValue() : null;
		Double lon = branch.getLongitude() != null ? branch.getLongitude().doubleValue() : null;

		return BranchResponse.builder().branchId(branch.getBranchId()).branchCode(branch.getBranchCode())
				.branchName(branch.getBranchName()).address(branch.getAddress()).latitude(lat).longitude(lon)
				.status(branch.getStatus()).createdAt(branch.getCreatedAt())
				.cameraCount(branch.getCameras() != null ? branch.getCameras().size() : 0).build();
	}
}