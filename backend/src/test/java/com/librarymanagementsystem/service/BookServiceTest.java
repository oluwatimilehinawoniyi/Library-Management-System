package com.librarymanagementsystem.service;


import com.librarymanagementsystem.commons.dto.BookDTO;
import com.librarymanagementsystem.commons.mapper.BookMapper;
import com.librarymanagementsystem.exception.BusinessLogicException;
import com.librarymanagementsystem.exception.DuplicateResourceException;
import com.librarymanagementsystem.exception.FileProcessingException;
import com.librarymanagementsystem.exception.ResourceNotFoundException;
import com.librarymanagementsystem.model.Book;
import com.librarymanagementsystem.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private BookDTO validBookDTO;
    private Book validBook;

    @BeforeEach
    void setUp() {
        validBookDTO = new BookDTO(
                null,
                "Nigerian Constitutional Law",
                "Ben Nwabueze",
                "978-9781234567",
                LocalDate.of(1982, 6, 15)
        );

        validBook = Book.builder()
                .id(1L)
                .title("Nigerian Constitutional Law")
                .author("Ben Nwabueze")
                .isbn("978-9781234567")
                .publishedDate(LocalDate.of(1982, 6, 15))
                .build();
    }


    @Test
    @DisplayName("Should create book successfully")
    void testCreateBook_Success() {
        when(bookRepository.existsByIsbn(validBookDTO.isbn())).thenReturn(false);
        when(bookMapper.toEntity(validBookDTO)).thenReturn(validBook);
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);
        when(bookMapper.toDTO(validBook)).thenReturn(validBookDTO);

        BookDTO result = bookService.createBook(validBookDTO);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(validBookDTO.title());
        verify(bookRepository).existsByIsbn(validBookDTO.isbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when creating book with duplicate ISBN")
    void testCreateBook_DuplicateISBN() {
        when(bookRepository.existsByIsbn(validBookDTO.isbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(validBookDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ISBN");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when creating book with future date")
    void testCreateBook_FutureDate() {
        BookDTO futureBookDTO = new BookDTO(
                null,
                "Future Book",
                "Time Traveler",
                "978-9999999999",
                LocalDate.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookService.createBook(futureBookDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("future");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should get book by ID successfully")
    void testGetBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));
        when(bookMapper.toDTO(validBook)).thenReturn(validBookDTO);

        BookDTO result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(validBookDTO.title());
        verify(bookRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when book not found")
    void testGetBookById_NotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book")
                .hasMessageContaining("999");

        verify(bookRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get all books with pagination")
    void testGetAllBooksPaginated_Success() {
        List<Book> books = Collections.singletonList(validBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(validBookDTO);

        Page<BookDTO> result = bookService.getAllBooksPaginated(0, 10, "id", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should handle invalid page parameters")
    void testGetAllBooksPaginated_InvalidParams() {
        List<Book> books = Collections.singletonList(validBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(validBookDTO);

        Page<BookDTO> result = bookService.getAllBooksPaginated(-1, -5, "id", "ASC");

        assertThat(result).isNotNull();
        verify(bookRepository).findAll(argThat((Pageable p) ->
                p.getPageNumber() == 0 && p.getPageSize() == 10
        ));
    }

    @Test
    @DisplayName("Should search books by keyword")
    void testSearchBooks_Success() {
        List<Book> books = Collections.singletonList(validBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.searchByTitleOrAuthorOrISBN(eq("Constitution"), any(PageRequest.class)))
                .thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(validBookDTO);

        Page<BookDTO> result = bookService.searchBooksPaginated("Constitution", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).searchByTitleOrAuthorOrISBN(eq("Constitution"), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should return all books when search keyword is empty")
    void testSearchBooks_EmptyKeyword() {
        List<Book> books = Collections.singletonList(validBook);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookPage);
        when(bookMapper.toDTO(any(Book.class))).thenReturn(validBookDTO);

        Page<BookDTO> result = bookService.searchBooksPaginated("", 0, 10);

        assertThat(result).isNotNull();
        verify(bookRepository).findAll(any(PageRequest.class));
        verify(bookRepository, never()).searchByTitleOrAuthorOrISBN(anyString(), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should update book successfully")
    void testUpdateBook_Success() {
        BookDTO updateDTO = new BookDTO(
                1L,
                "Updated Title",
                "Updated Author",
                "978-9781234567", // Same ISBN
                LocalDate.of(1982, 6, 15)
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);
        when(bookMapper.toDTO(validBook)).thenReturn(updateDTO);

        BookDTO result = bookService.updateBook(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(bookRepository).findById(1L);
        verify(bookMapper).updateEntityFromDTO(updateDTO, validBook);
        verify(bookRepository).save(validBook);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent book")
    void testUpdateBook_NotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(999L, validBookDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with duplicate ISBN")
    void testUpdateBook_DuplicateISBN() {
        BookDTO updateDTO = new BookDTO(
                1L,
                "Updated Title",
                "Updated Author",
                "978-9999999999", // Different ISBN
                LocalDate.of(1982, 6, 15)
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));
        when(bookRepository.existsByIsbn("978-9999999999")).thenReturn(true);

        assertThatThrownBy(() -> bookService.updateBook(1L, updateDTO))
                .isInstanceOf(DuplicateResourceException.class);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw exception when updating with future date")
    void testUpdateBook_FutureDate() {
        BookDTO futureUpdateDTO = new BookDTO(
                1L,
                "Updated Title",
                "Updated Author",
                "978-9781234567",
                LocalDate.now().plusDays(1)
        );

        when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));

        assertThatThrownBy(() -> bookService.updateBook(1L, futureUpdateDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("future");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw BusinessLogicException when published date is in the future")
    void testCreateBook_FutureDate_ThrowsException() {
        BookDTO futureBookDTO = new BookDTO(
                null,
                validBookDTO.title(),
                validBookDTO.author(),
                validBookDTO.isbn(),
                LocalDate.now().plusDays(1)
        );

        assertThatThrownBy(() -> bookService.createBook(futureBookDTO))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("Published date cannot be in the future");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete book successfully")
    void testDeleteBook_Success() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        bookService.deleteBook(1L);

        verify(bookRepository).existsById(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent book")
    void testDeleteBook_NotFound() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get library statistics")
    void testGetLibraryStats_Success() {
        List<String> authors = Arrays.asList("Author 1", "Author 2");
        List<Object[]> yearData = Arrays.asList(
                new Object[]{2020, 5L},
                new Object[]{2021, 3L}
        );
        List<Book> sortedBooks = Collections.singletonList(validBook);

        when(bookRepository.count()).thenReturn(10L);
        when(bookRepository.findAllUniqueAuthors()).thenReturn(authors);
        when(bookRepository.countBooksByYear()).thenReturn(yearData);

        Map<String, Object> stats = bookService.getLibraryStats();

        assertThat(stats).isNotNull();
        assertThat(stats).containsKeys("totalBooks", "uniqueAuthorsCount", "booksByYear");
        assertThat(stats.get("totalBooks")).isEqualTo(10L);
        assertThat(stats.get("uniqueAuthorsCount")).isEqualTo(2);

        verify(bookRepository).count();
        verify(bookRepository).findAllUniqueAuthors();
        verify(bookRepository).countBooksByYear();
    }

    @Test
    @DisplayName("Should handle empty library statistics")
    void testGetLibraryStats_EmptyLibrary() {
        when(bookRepository.count()).thenReturn(0L);
        when(bookRepository.findAllUniqueAuthors()).thenReturn(List.of());
        when(bookRepository.countBooksByYear()).thenReturn(List.of());

        Map<String, Object> stats = bookService.getLibraryStats();

        assertThat(stats).isNotNull();
        assertThat(stats.get("totalBooks")).isEqualTo(0L);
        assertThat(stats.get("uniqueAuthorsCount")).isEqualTo(0);
        assertThat(stats).doesNotContainKey("oldestBook");
        assertThat(stats).doesNotContainKey("newestBook");
    }

    @Test
    @DisplayName("Should get stats with oldest and newest books")
    void testGetLibraryStats_WithOldestNewest() {
        when(bookRepository.count()).thenReturn(10L);
        when(bookRepository.findAllUniqueAuthors()).thenReturn(List.of("Author 1"));
        when(bookRepository.countBooksByYear()).thenReturn(List.of());
        when(bookRepository.findTopByOrderByPublishedDateAsc())
                .thenReturn(Optional.of(validBook));
        when(bookRepository.findTopByOrderByPublishedDateDesc())
                .thenReturn(Optional.of(validBook));

        Map<String, Object> stats = bookService.getLibraryStats();

        assertThat(stats).containsKeys("oldestBook", "newestBook");
    }

    @Test
    @DisplayName("Should handle FileProcessingException during CSV import")
    void testImportBooksFromCsv_FileProcessingException() throws FileProcessingException, IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new IOException("Failed to read CSV file"));

        assertThatThrownBy(() -> bookService.bulkCreateBooks(mockFile))
                .isInstanceOf(FileProcessingException.class)
                .hasMessageContaining("Failed to read CSV file");
    }

    @Test
    @DisplayName("Should successfully import valid CSV")
    void testBulkImport_Success() throws Exception {
        String csvContent = "title,author,isbn,publishedDate\n" +
                "Test Book,Test Author,978-1234567890,2020-01-01";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "books.csv",
                "text/csv",
                csvContent.getBytes()
        );

        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        Map<String, Object> result = bookService.bulkCreateBooks(file);

        assertThat(result.get("successCount")).isEqualTo(1);
        assertThat(result.get("failureCount")).isEqualTo(0);
    }

}