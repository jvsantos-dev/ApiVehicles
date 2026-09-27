package com.ford.vehicleapi.service;

import com.ford.vehicleapi.domain.VehicleCategory;
import com.ford.vehicleapi.domain.VehicleModel;
import com.ford.vehicleapi.dto.model.VehicleModelRequest;
import com.ford.vehicleapi.dto.model.VehicleModelResponse;
import com.ford.vehicleapi.exception.ConflictException;
import com.ford.vehicleapi.exception.ResourceNotFoundException;
import com.ford.vehicleapi.repository.TestDriveRepository;
import com.ford.vehicleapi.repository.VehicleModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleModelService {

    private final VehicleModelRepository modelRepository;
    private final TestDriveRepository testDriveRepository;

    public VehicleModelService(VehicleModelRepository modelRepository, TestDriveRepository testDriveRepository) {
        this.modelRepository = modelRepository;
        this.testDriveRepository = testDriveRepository;
    }

    @Transactional(readOnly = true)
    public List<VehicleModelResponse> findAll(VehicleCategory category) {
        List<VehicleModel> models = category == null
                ? modelRepository.findAllByOrderByNameAsc()
                : modelRepository.findByCategoryOrderByNameAsc(category);
        return models.stream().map(VehicleModelResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VehicleModelResponse findById(Long id) {
        return VehicleModelResponse.from(getModel(id));
    }

    @Transactional
    public VehicleModelResponse create(VehicleModelRequest request) {
        String name = request.name().trim();
        if (modelRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Já existe um modelo com o nome " + name);
        }
        VehicleModel model = new VehicleModel(name, request.category(), request.description(),
                request.launchYear(), request.activeOrDefault());
        return VehicleModelResponse.from(modelRepository.save(model));
    }

    @Transactional
    public VehicleModelResponse update(Long id, VehicleModelRequest request) {
        VehicleModel model = getModel(id);
        String name = request.name().trim();
        if (modelRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Já existe um modelo com o nome " + name);
        }
        model.update(name, request.category(), request.description(), request.launchYear(), request.activeOrDefault());
        return VehicleModelResponse.from(model);
    }

    @Transactional
    public void delete(Long id) {
        VehicleModel model = getModel(id);
        if (testDriveRepository.existsByVersionModelId(id)) {
            throw new ConflictException("O modelo possui test drives agendados e não pode ser removido. Desative-o (active=false).");
        }
        modelRepository.delete(model);
    }

    VehicleModel getModel(Long id) {
        return modelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modelo", id));
    }
}
