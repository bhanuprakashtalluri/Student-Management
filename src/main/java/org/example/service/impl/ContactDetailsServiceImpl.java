package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.entity.ContactDetails;
import org.example.repository.ContactDetailsRepository;
import org.example.service.ContactDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactDetailsServiceImpl implements ContactDetailsService {
    private final ContactDetailsRepository repository;

    @Override
    public ContactDetails createContact(ContactDetails contact) { return repository.save(contact); }

    @Override
    public ContactDetails getContactById(Long id) { return repository.findById(id).orElse(null); }

    @Override
    public List<ContactDetails> getAllContacts() { return repository.findAll(); }

    @Override
    public ContactDetails updateContact(Long id, ContactDetails updated) {
        Optional<ContactDetails> existing = repository.findById(id);
        if (existing.isEmpty()) return null;
        updated.setContactNumber(id);
        return repository.save(updated);
    }

    @Override
    public void deleteContact(Long id) { repository.deleteById(id); }
}
