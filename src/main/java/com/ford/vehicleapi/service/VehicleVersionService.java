package com.ford.vehicleapi.service;

import com.ford.vehicleapi.domain.VehicleModel;
import com.ford.vehicleapi.domain.VehicleVersion;
import com.ford.vehicleapi.dto.version.VehicleVersionRequest;
import com.ford.vehicleapi.dto.version.VehicleVersionResponse;
import com.ford.vehicleapi.exception.ConflictException;
import com.ford.vehicleapi.exception.ResourceNotFoundException;
import com.ford.vehicleapi.repository.TestDriveRepository;
import com.ford.vehicleapi.repository.VehicleVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleVersionService {

    private final VehicleModelService modelService;
    private final VehicleVersionRepository versionRepository;
    private final TestDriveRepository testDriveRepository;

    public VehicleVersionService(VehicleModelService modelService, VehicleVersionRepository versionRepository,
                                 TestDriveRepository testDriveRepository) {
        this.modelService = modelService;
        this.versionRepository = versionRepository;
        this.testDriveRepository = testDriveRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleVersionResponse> findByModel(Long modelId) {
        modelService.getModel(modelId);
        return versionRepository.findByModelIdOrderByPriceAsc(modelId).stream()
                .map(VehicleVersionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleVersionResponse findById(Long modelId, Long versionId) {
        return VehicleVersionResponse.from(getVersion(modelId, versionId));
    }

    @Transactional
    public VehicleVersionResponse create(Long modelId, VehicleVersionRequest request) {
        VehicleModel model = modelService.getModel(modelId);
        String name = request.name().trim();
        if (versionRepository.existsByModelIdAndNameIgnoreCaseAndModelYear(modelId, name, request.modelYear())) {
            throw duplicated(model, name, request.modelYear());
        }
        VehicleVersion version = model.addVersion(new VehicleVersion(name, request.modelYear(), request.engine(),
                request.horsepower(), request.transmission(), request.fuelType(), request.price()));
        return VehicleVersionResponse.from(versionRepository.save(version));
    }

    @Transactional
    public VehicleVersionResponse update(Long modelId, Long versionId, VehicleVersionRequest request) {
        VehicleVersion version = getVersion(modelId, versionId);
        String name = request.name().trim();
        if (versionRepository.existsByModelIdAndNameIgnoreCaseAndModelYearAndIdNot(modelId, name, request.modelYear(), versionId)) {
            throw duplicated(version.getModel(), name, request.modelYear());
        }
        version.update(name, request.modelYear(), request.engine(), request.horsepower(),
                request.transmission(), request.fuelType(), request.price());
        return VehicleVersionResponse.from(version);
    }

    @Transactional
    public void delete(Long modelId, Long versionId) {
        VehicleVersion version = getVersion(modelId, versionId);
        if (testDriveRepository.existsByVersionId(versionId)) {
            throw new ConflictException("A versão possui test drives agendados e não pode ser removida");
        }
        version.getModel().removeVersion(version);
        versionRepository.delete(version);
    }

    VehicleVersion getVersion(Long modelId, Long versionId) {
        modelService.getModel(modelId);
        return versionRepository.findByIdAndModelId(versionId, modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Versão", versionId));
    }

    private ConflictException duplicated(VehicleModel model, String name, Integer year) {
        return new ConflictException("O modelo %s já possui a versão %s %d".formatted(model.getName(), name, year));
    }
}
