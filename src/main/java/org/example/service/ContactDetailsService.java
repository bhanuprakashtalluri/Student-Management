package org.example.service;

import org.example.entity.ContactDetails;
import java.util.List;

public interface ContactDetailsService {
    ContactDetails createContact(ContactDetails contact);
    ContactDetails getContactById(Long id);
    List<ContactDetails> getAllContacts();
    ContactDetails updateContact(Long id, ContactDetails updated);
    void deleteContact(Long id);
}
