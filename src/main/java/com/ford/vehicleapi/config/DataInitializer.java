package com.ford.vehicleapi.config;

import com.ford.vehicleapi.domain.FuelType;
import com.ford.vehicleapi.domain.Role;
import com.ford.vehicleapi.domain.User;
import com.ford.vehicleapi.domain.VehicleCategory;
import com.ford.vehicleapi.domain.VehicleModel;
import com.ford.vehicleapi.domain.VehicleVersion;
import com.ford.vehicleapi.repository.UserRepository;
import com.ford.vehicleapi.repository.VehicleModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Popula o banco com usuários de exemplo (um por perfil) e o catálogo inicial Ford. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final VehicleModelRepository modelRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, VehicleModelRepository modelRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelRepository = modelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedCatalog();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new User("Administrador Ford", "admin@ford.com", passwordEncoder.encode("Admin@123"), Role.ADMIN));
        userRepository.save(new User("Concessionária Ford Centro", "dealer@ford.com", passwordEncoder.encode("Dealer@123"), Role.DEALER));
        userRepository.save(new User("Cliente Exemplo", "customer@ford.com", passwordEncoder.encode("Customer@123"), Role.CUSTOMER));
        log.info("Usuários de exemplo criados: admin@ford.com, dealer@ford.com, customer@ford.com");
    }

    private void seedCatalog() {
        if (modelRepository.count() > 0) {
            return;
        }

        VehicleModel ranger = model("Ranger", VehicleCategory.PICKUP, 1983,
                "Picape média com tração 4x4, referência em robustez e capacidade off-road.");
        ranger.addVersion(version("XLS", 2026, "2.0 Turbo Diesel", 170, "Automática 6 marchas", FuelType.DIESEL, "265990.00"));
        ranger.addVersion(version("Limited", 2026, "3.0 V6 Turbo Diesel", 250, "Automática 10 marchas", FuelType.DIESEL, "339990.00"));
        ranger.addVersion(version("Raptor", 2026, "3.0 V6 Biturbo", 397, "Automática 10 marchas", FuelType.GASOLINE, "469990.00"));

        VehicleModel mustang = model("Mustang", VehicleCategory.SPORTS, 1964,
                "O pony car original: esportivo com motor V8 aspirado.");
        mustang.addVersion(version("GT Performance", 2026, "5.0 V8 Coyote", 488, "Automática 10 marchas", FuelType.GASOLINE, "549000.00"));
        mustang.addVersion(version("Dark Horse", 2026, "5.0 V8 Coyote", 507, "Automática 10 marchas", FuelType.GASOLINE, "649000.00"));

        VehicleModel mustangMachE = model("Mustang Mach-E", VehicleCategory.SUV, 2020,
                "SUV 100% elétrico com DNA Mustang e tração integral.");
        mustangMachE.addVersion(version("GT Performance", 2026, "Dois motores elétricos", 487, "Redução direta", FuelType.ELECTRIC, "486000.00"));

        VehicleModel broncoSport = model("Bronco Sport", VehicleCategory.SUV, 2020,
                "SUV compacto aventureiro com tração 4x4 e modos de terreno G.O.A.T.");
        broncoSport.addVersion(version("Wildtrak", 2026, "2.0 EcoBoost", 253, "Automática 8 marchas", FuelType.GASOLINE, "289990.00"));

        VehicleModel maverick = model("Maverick", VehicleCategory.PICKUP, 2021,
                "Picape intermediária com opção híbrida e monobloco.");
        maverick.addVersion(version("Lariat", 2026, "2.0 EcoBoost", 253, "Automática 8 marchas", FuelType.GASOLINE, "239990.00"));
        maverick.addVersion(version("Lariat Hybrid", 2026, "2.5 Híbrido", 194, "CVT", FuelType.HYBRID, "249990.00"));

        VehicleModel territory = model("Territory", VehicleCategory.SUV, 2020,
                "SUV médio com foco em conforto e tecnologia.");
        territory.addVersion(version("Titanium", 2026, "1.5 EcoBoost", 169, "Automatizada 7 marchas (DCT)", FuelType.GASOLINE, "214990.00"));

        VehicleModel f150 = model("F-150", VehicleCategory.PICKUP, 1975,
                "Picape full-size mais vendida dos EUA por décadas.");
        f150.addVersion(version("Lariat", 2026, "5.0 V8", 405, "Automática 10 marchas", FuelType.GASOLINE, "509990.00"));
        f150.addVersion(version("Raptor", 2026, "3.5 V6 EcoBoost Biturbo", 456, "Automática 10 marchas", FuelType.GASOLINE, "699990.00"));

        VehicleModel transit = model("Transit", VehicleCategory.VAN, 1965,
                "Van comercial para carga e passageiros.");
        transit.addVersion(version("Furgão", 2026, "2.2 Turbo Diesel", 125, "Manual 6 marchas", FuelType.DIESEL, "299990.00"));

        // Modelo fora de linha: aparece no catálogo, mas não aceita novos test drives.
        VehicleModel ka = new VehicleModel("Ka", VehicleCategory.HATCH,
                "Hatch compacto, produzido no Brasil até 2021.", 1996, false);
        ka.addVersion(version("SE", 2021, "1.0 Ti-VCT", 75, "Manual 5 marchas", FuelType.FLEX, "69990.00"));

        modelRepository.save(ranger);
        modelRepository.save(mustang);
        modelRepository.save(mustangMachE);
        modelRepository.save(broncoSport);
        modelRepository.save(maverick);
        modelRepository.save(territory);
        modelRepository.save(f150);
        modelRepository.save(transit);
        modelRepository.save(ka);
        log.info("Catálogo Ford inicial carregado com {} modelos", modelRepository.count());
    }

    private static VehicleModel model(String name, VehicleCategory category, int launchYear, String description) {
        return new VehicleModel(name, category, description, launchYear, true);
    }

    private static VehicleVersion version(String name, int year, String engine, int hp, String transmission,
                                          FuelType fuelType, String price) {
        return new VehicleVersion(name, year, engine, hp, transmission, fuelType, new BigDecimal(price));
    }
}
