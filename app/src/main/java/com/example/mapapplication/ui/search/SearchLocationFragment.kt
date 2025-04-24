package com.example.mapapplication.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mapapplication.R
import com.example.mapapplication.databinding.FragmentSearchLocationBinding
import com.example.mapapplication.model.LocationSearched
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchLocationFragment : Fragment() {
    private var _binding: FragmentSearchLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchLocationViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI()
        observe()
    }

    private fun setUpUI() {
        binding.searchBar.setStartIconOnClickListener {
            findNavController().popBackStack()
        }

        binding.edtSearch.addTextChangedListener {
            viewModel.query.value = it.toString()
        }

        binding.edtSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val input = binding.edtSearch.text.toString()
                Log.d("SearchLocation", "search: $input")
                viewModel.search(input)
                true
            }
            false
        }

        val recyclerView = binding.listItem
        val adapter = LocationAdapter {
            Log.d("SearchLocation", "onItemClick: $it")
            findNavController().navigate(
                R.id.locationInforFragment,
                args = bundleOf(
                    "place_id" to it.placeId,
                )
            )
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.locationSearched.collectLatest {
                Log.d("SearchLocation", "locationSearched: $it")
                binding.widgetSessionPlaceType.root.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                (binding.listItem.adapter as LocationAdapter).submitList(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest {
                binding.pgLoading.visibility = if (it) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}