package planning;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
class OutingController {

	private final OutingRepository repository;
	Logger log = LoggerFactory.getLogger(OutingController.class);
	@Value("${STAFF_DNS:empty}")
	private String configMapStaffDns;
	@Value("${STAFF_PORT:empty}")
	private String configMapStaffPort;


	OutingController(OutingRepository repository) {
		this.repository = repository;
	}
	
	@GetMapping("/ping")
	String ping() {
		return "frontend";
	}

	@GetMapping("/health")
	String getHealth() {
		
		log.info("ConfigMap Dns: " + this.configMapStaffDns);
		log.info("ConfigMap Port: " + this.configMapStaffPort);
		String resourceUrl = "http://" + configMapStaffDns + ":" + configMapStaffPort + "/health";

		log.info("Calling health url: " + resourceUrl);
		
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(resourceUrl, String.class);
		return response.getBody();
	}
	
	// Aggregate root
	@GetMapping("/outings")
	List<Outing> all() {
		return repository.findAll();
	}

	@PostMapping("/outings")
	Outing newEmployee(@RequestBody Outing newOuting) {
		return repository.save(newOuting);
	}

	// Single item

	@GetMapping("/employees/{id}")
	Outing one(@PathVariable Long id) {

		return repository.findById(id)
			.orElseThrow(() -> new OutingNotFoundException(id));
	}

	@PutMapping("/employees/{id}")
	Outing replaceEmployee(@RequestBody Outing newEmployee, @PathVariable Long id) {

		return repository.findById(id)
			.map(employee -> {
				employee.setName(newEmployee.getName());
				employee.setRole(newEmployee.getRole());
				return repository.save(employee);
			})
			.orElseGet(() -> {
				newEmployee.setId(id);
				return repository.save(newEmployee);
			});
	}

	@DeleteMapping("/employees/{id}")
	void deleteEmployee(@PathVariable Long id) {
		repository.deleteById(id);
	}
}