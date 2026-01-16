package com.librarymanagementsystem.repository;

import com.librarymanagementsystem.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("""
                SELECT b FROM Book b
                WHERE
                  LOWER(b.title)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR b.isbn LIKE CONCAT('%', :keyword, '%')
            """)
    Page<Book> searchByTitleOrAuthorOrISBN(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT b.author FROM Book b ORDER BY b.author")
    List<String> findAllUniqueAuthors();

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<Book> findByPublishedDateAfter(LocalDate date);

    @Query("""
            SELECT YEAR(b.publishedDate) as year, COUNT(b) as count
            FROM Book b GROUP BY YEAR(b.publishedDate)
            ORDER BY YEAR(b.publishedDate) DESC
            """)
    List<Object[]> countBooksByYear();

    Optional<Book> findTopByOrderByPublishedDateAsc();

    Optional<Book> findTopByOrderByPublishedDateDesc();
}
