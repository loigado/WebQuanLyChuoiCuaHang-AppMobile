package com.branchmanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[UserShift]")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_shift_id")
	private Integer userShiftId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shift_id", nullable = false)
	private Shift shift;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "status")
	private String status; // assigned | completed | absent

	@Column(name = "note")
	private String note;

	@Column(name = "assigned_at")
	private LocalDateTime assignedAt;
}