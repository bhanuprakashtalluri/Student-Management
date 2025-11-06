package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.entity.AddressDetails;
import org.example.repository.AddressDetailsRepository;
import org.example.service.AddressDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressDetailsServiceImpl implements AddressDetailsService {
    private final AddressDetailsRepository repository;

    @Override
    public AddressDetails createAddress(AddressDetails address) { return repository.save(address); }

    @Override
    public AddressDetails getAddressById(Long id) { return repository.findById(id).orElse(null); }

    @Override
    public List<AddressDetails> getAllAddresses() { return repository.findAll(); }

    @Override
    public AddressDetails updateAddress(Long id, AddressDetails updated) {
        Optional<AddressDetails> existing = repository.findById(id);
        if (existing.isEmpty()) return null;
        updated.setAddressNumber(id);
        return repository.save(updated);
    }

    @Override
    public void deleteAddress(Long id) { repository.deleteById(id); }
}
