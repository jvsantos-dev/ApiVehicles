package com.ford.vehicleapi.service;

import com.ford.vehicleapi.domain.TestDrive;
import com.ford.vehicleapi.domain.TestDriveStatus;
import com.ford.vehicleapi.domain.User;
import com.ford.vehicleapi.domain.VehicleVersion;
import com.ford.vehicleapi.dto.testdrive.TestDriveRequest;
import com.ford.vehicleapi.dto.testdrive.TestDriveResponse;
import com.ford.vehicleapi.exception.BusinessRuleException;
import com.ford.vehicleapi.exception.ResourceNotFoundException;
import com.ford.vehicleapi.repository.TestDriveRepository;
import com.ford.vehicleapi.repository.UserRepository;
import com.ford.vehicleapi.repository.VehicleVersionRepository;
import com.ford.vehicleapi.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de test drive. Além da autorização por perfil (SecurityConfig), aqui é aplicada
 * a autorização por propriedade: um CUSTOMER só enxerga/cancela os próprios agendamentos.
 */
@Service
public class TestDriveService {

    private final TestDriveRepository testDriveRepository;
    private final VehicleVersionRepository versionRepository;
    private final UserRepository userRepository;

    public TestDriveService(TestDriveRepository testDriveRepository, VehicleVersionRepository versionRepository,
                            UserRepository userRepository) {
        this.testDriveRepository = testDriveRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TestDriveResponse> findAll(CurrentUser currentUser) {
        List<TestDrive> testDrives = currentUser.isStaff()
                ? testDriveRepository.findAllByOrderByScheduledAtAsc()
                : testDriveRepository.findByCustomerIdOrderByScheduledAtAsc(currentUser.id());
        return testDrives.stream().map(TestDriveResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TestDriveResponse findById(Long id, CurrentUser currentUser) {
        return TestDriveResponse.from(getAccessible(id, currentUser));
    }

    @Transactional
    public TestDriveResponse create(TestDriveRequest request, CurrentUser currentUser) {
        User customer = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", currentUser.id()));
        VehicleVersion version = versionRepository.findById(request.versionId())
                .orElseThrow(() -> new ResourceNotFoundException("Versão", request.versionId()));
        if (!version.getModel().isActive()) {
            throw new BusinessRuleException("O modelo %s não está mais disponível para test drive"
                    .formatted(version.getModel().getName()));
        }
        TestDrive testDrive = new TestDrive(customer, version, request.scheduledAt(),
                request.dealership().trim(), request.notes());
        return TestDriveResponse.from(testDriveRepository.save(testDrive));
    }

    @Transactional
    public TestDriveResponse updateStatus(Long id, TestDriveStatus newStatus) {
        TestDrive testDrive = getTestDrive(id);
        if (!testDrive.getStatus().canTransitionTo(newStatus)) {
            throw new BusinessRuleException("Não é possível alterar o status de %s para %s. Transições permitidas: %s"
                    .formatted(testDrive.getStatus(), newStatus, testDrive.getStatus().allowedTransitions()));
        }
        testDrive.setStatus(newStatus);
        return TestDriveResponse.from(testDrive);
    }

    @Transactional
    public void delete(Long id, CurrentUser currentUser) {
        TestDrive testDrive = getAccessible(id, currentUser);
        if (!currentUser.isAdmin() && testDrive.getStatus() != TestDriveStatus.REQUESTED) {
            throw new BusinessRuleException("Somente agendamentos com status REQUESTED podem ser removidos");
        }
        testDriveRepository.delete(testDrive);
    }

    private TestDrive getAccessible(Long id, CurrentUser currentUser) {
        TestDrive testDrive = getTestDrive(id);
        if (!currentUser.isStaff() && !testDrive.isOwnedBy(currentUser.id())) {
            throw new AccessDeniedException("Test drive pertence a outro cliente");
        }
        return testDrive;
    }

    private TestDrive getTestDrive(Long id) {
        return testDriveRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Test drive", id));
    }
}
